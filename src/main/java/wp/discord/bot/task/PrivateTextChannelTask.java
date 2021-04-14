package wp.discord.bot.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.ActionHandler;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.bot.BotAction;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.Reply;

@Component
public class PrivateTextChannelTask implements ActionHandler {

	@Autowired
	private JDA jda;

	@Override
	public void handleAction(BotAction action) throws Exception {
		String userMention = action.getEntities().get(CmdEntity.USER);
		String userId = DiscordFormat.extractId(userMention);

		User user = jda.retrieveUserById(userId).complete();
		if (user == null) {
			Reply reply = Reply.of().literal("Invalid user ").code(userMention).newline( )//
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new BotException(reply);
		}

		PrivateChannel channel = user.openPrivateChannel().complete();
		if (channel == null) {
			Reply reply = Reply.of().literal("Invalid text-channel ").mentionUser(userId).newline() //
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new BotException(reply);
		}

		String message = action.getEntities().get(CmdEntity.MESSAGE);
		if (StringUtils.isEmpty(message)) {
			Reply reply = Reply.of().literal("Empty text-message ").newline() //
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new BotException(reply);
		}

		channel.sendMessage(message).queue();
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.SEND_MESSAGE_TO_PRIVATE_CHANNEL;
	}

}
