package wp.discord.bot.task.delete;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.constant.Reaction;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.EventUtil;
import wp.discord.bot.util.Reply;

@Component
@Slf4j
public class DeleteMessageTask {

	@Autowired
	private JDA jda;

	@Autowired
	private TracingHandler tracing;

	public void deleteBotMessage(BotAction action) throws Exception {
		String messageId = getMessageId(action);
		MessageChannel mc = getMessageChannel(action);

		mc.retrieveMessageById(messageId).queue(tracing.trace((m) -> {
			
			log.debug("User {} found message id: {}, channel:{}, content: \n{}", action.getAuthorId(), messageId, mc, m.getContentRaw());
			
			m.delete().queue(tracing.trace((voidObj) -> {
				addReactionOnSuccess(action);
			}), tracing.trace((e) -> {
				log.error("User {} failed to delete message id: {}", action.getAuthorId(), messageId, e);
			}));

		}), tracing.trace((e) -> {
			
			Reply reply = Reply.of().literal("Not found message ID: ").code(messageId);
			tracing.queue(action.getEventMessageChannel().sendMessage(reply.toString()));
		}));
	}

	private String getMessageId(BotAction action) throws Exception {
		String messageId = DiscordFormat.extractId(action.getFirstTokenParam(CmdToken.ID));
		if (StringUtils.isEmpty(messageId)) {
			Reply reply = Reply.of().bold("Error! ").literal("Message ID is required!").newline()//
					.code("Usage: ").code("bot delete message [channel <channel-id>] id <message-id>");
			throw new ActionFailException(reply);
		}
		return messageId;
	}

	public MessageChannel getMessageChannel(BotAction action) throws Exception {
		MessageChannel mc = null;
		String channelId = DiscordFormat.extractId(action.getFirstTokenParam(CmdToken.CHANNEL));
		if (StringUtils.isEmpty(channelId)) {
			mc = action.getEventMessageChannel();
			log.debug("Use default message channel: {}", mc);

		} else {
			mc = getMessageChannel(channelId);
			if (mc == null) {
				Reply reply = Reply.of().literal("Not found Channel ID: ").code(channelId);
				throw new ActionFailException(reply);
			}
		}

		if (mc instanceof PrivateChannel) {
			PrivateChannel pc = (PrivateChannel) mc;
			User user = pc.getUser();
			log.debug("deleting message in private channel: {} of user: {}", mc, user);

			if (!user.getId().equals(action.getAuthorId())) {
				log.error("attempt to delete message in other user private channel!");
				Reply reply = Reply.of().literal("Not found channel-id: ").code(channelId);
				throw new ActionFailException(reply);
			}
		}

		return mc;
	}

	private MessageChannel getMessageChannel(String channelId) {
		if (StringUtils.isEmpty(channelId)) {
			return null;
		}

		TextChannel tc = jda.getTextChannelById(channelId);
		if (tc != null) {
			return tc;
		}

		PrivateChannel mc = jda.getPrivateChannelById(channelId);
		if (mc != null) {
			return mc;
		}
		return null;
	}

	private void addReactionOnSuccess(BotAction action) {
		Message m = EventUtil.getMessage(action.getEvent());
		if (m == null) {
			return;
		}

		m.addReaction(Reaction.CHECKED.getCode()).queue();
	}

}
