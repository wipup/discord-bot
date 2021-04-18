package wp.discord.bot.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.core.bot.UserManager;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;

@Component
public class PrivateTextChannelTask implements ActionHandler {

	@Autowired
	private UserManager userManager;

	@Override
	public void handleAction(BotAction action) throws Exception {
		String userId = userManager.getUserEntityId(action);

		User user = userManager.getUserEntity(userId);
		if (user == null) {
			Reply reply = Reply.of().literal("Invalid user ").code(userId).newline()//
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new BotException(reply);
		}

		PrivateChannel channel = user.openPrivateChannel().complete();
		if (channel == null) {
			Reply reply = Reply.of().literal("Invalid text-channel ").mentionUser(userId).newline() //
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new BotException(reply);
		}

		String message = action.getFirstEntitiesParam(CmdEntity.MESSAGE);
		if (StringUtils.isEmpty(message)) {
			Reply reply = Reply.of().literal("Empty text-message ").newline() //
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new BotException(reply);
		}

		channel.sendMessage(message).queue((m) -> {
			m.suppressEmbeds(true).queue();
		});
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.SEND_MESSAGE_TO_PRIVATE_CHANNEL;
	}

}
