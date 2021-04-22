package wp.discord.bot.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.core.bot.UserManager;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;

@Component
public class PrivateTextChannelActionHandler implements ActionHandler {

	@Autowired
	private UserManager userManager;

	@Autowired
	private TracingHandler tracing;

	@Override
	public void handleAction(BotAction action) throws Exception {
		String userId = userManager.getUserEntityId(action);
		User user = getUser(action, userId);
		PrivateChannel channel = getPrivateChannel(action, userId, user);
		String message = getMessage(action);

		channel.sendMessage(message).queue(tracing.trace((m) -> {
			m.suppressEmbeds(true).queue();
		}));
	}

	private String getMessage(BotAction action) throws Exception {
		String message = action.getFirstTokenParam(CmdToken.MESSAGE);
		if (StringUtils.isEmpty(message)) {
			Reply reply = Reply.of().literal("Empty text-message ").newline() //
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new ActionFailException(reply);
		}
		return message;
	}

	private PrivateChannel getPrivateChannel(BotAction action, String userId, User user) throws Exception {
		PrivateChannel channel = user.openPrivateChannel().complete();
		if (channel == null) {
			Reply reply = Reply.of().literal("Invalid text-channel ").mentionUser(userId).newline() //
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new ActionFailException(reply);
		}
		return channel;
	}

	private User getUser(BotAction action, String userId) throws Exception {
		User user = userManager.getUserEntity(userId);
		if (user == null) {
			Reply reply = Reply.of().literal("Invalid user ").code(userId).newline()//
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new ActionFailException(reply);
		}
		return user;
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.SEND_MESSAGE_TO_PRIVATE_CHANNEL;
	}

}
