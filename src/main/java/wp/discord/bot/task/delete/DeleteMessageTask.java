package wp.discord.bot.task.delete;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.Reply;

@Component
@Slf4j
public class DeleteMessageTask {

	@Autowired
	private JDA jda;

	public void deleteBotMessage(BotAction action) throws Exception {
		String messageId = DiscordFormat.extractId(action.getFirstEntitiesParam(CmdEntity.ID));
		if (StringUtils.isEmpty(messageId)) {
			Reply reply = Reply.of().bold("Error! ").literal("Message ID is required!").newline()//
					.code("Usage: ").code("bot delete message [channel <channel-id>] id <message-id>");
			action.getEventMessageChannel().sendMessage(reply.toString()).queue();
			return;
		}

		MessageChannel mc = null;
		String channelId = DiscordFormat.extractId(action.getFirstEntitiesParam(CmdEntity.CHANNEL));
		if (StringUtils.isEmpty(channelId)) {
			mc = action.getEventMessageChannel();
			log.debug("Use default message channel: {}", mc);

		} else {
			mc = getMessageChannel(channelId);
			if (mc == null) {
				Reply reply = Reply.of().literal("Not found channel: ").code(channelId);
				action.getEventMessageChannel().sendMessage(reply.toString()).queue();
				return;
			}
		}

		mc.retrieveMessageById(messageId).queue((m) -> {
			log.debug("User {} found message id: {}, content: \n{}", action.getAuthorId(), messageId, m.getContentRaw());
			m.delete().queue(null, (e) -> {
				log.error("User {} failed to delete message id: {}", action.getAuthorId(), messageId, e);
			});
		}, (e) -> {
			Reply reply = Reply.of().literal("Not found message ID: ").code(messageId);
			action.getEventMessageChannel().sendMessage(reply.toString()).queue();
		});
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

}
