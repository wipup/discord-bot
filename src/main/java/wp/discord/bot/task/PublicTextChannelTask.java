package wp.discord.bot.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.ActionHandler;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.bot.BotAction;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.Reply;

@Component
public class PublicTextChannelTask implements ActionHandler {

	@Autowired
	private JDA jda;

	@Override
	public void handleAction(BotAction action) throws Exception {
		String channelMention = action.getEntities().get(CmdEntity.CHANNEL);
		String channelId = DiscordFormat.extractId(channelMention);

		TextChannel channel = jda.getTextChannelById(channelId);
		if (channel == null) {
			Reply reply = Reply.of().literal("Invalid text-channel ").code(channelMention).newline()//
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new BotException(reply);
		}

		String message = action.getEntities().get(CmdEntity.MESSAGE);
		if (StringUtils.isEmpty(message)) {
			Reply reply = Reply.of().literal("Empty text-message ").code(channelMention).newline()//
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new BotException(reply);
		}

		channel.sendMessage(message).queue();
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.SEND_MESSAGE_TO_TEXT_CHANNEL;
	}

}
