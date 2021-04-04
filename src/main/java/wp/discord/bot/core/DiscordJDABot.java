package wp.discord.bot.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.events.GenericEvent;
import wp.discord.bot.core.robot.RobotCore;
import wp.discord.bot.core.robot.RobotCoreManager;
import wp.discord.bot.locale.MessageKey;
import wp.discord.bot.locale.MessageLanguageResolver;
import wp.discord.bot.model.CommandContext;

@Component
@Slf4j
public class DiscordJDABot implements ThreadContextAware {

	@Autowired
	private MessageLanguageResolver languageResolver;

	@Autowired
	private RobotCoreManager manager;

	public <T extends GenericEvent> void newRobot(T event) {
		RobotCore robot = manager.newRobot(event);
		getCurrentContext().setRobot(robot);
		getCurrentContext().setCommandContext(new CommandContext());
	}

	public boolean canAccept(String event) {
		RobotCore robot = getCurrentContext().getRobot();
		return robot.canAccept(event);
	}

	public void fire(String event) {
		RobotCore robot = getCurrentContext().getRobot();
		robot.fire(event, getCurrentContext().getCommandContext());
	}

	public boolean canFinish() {
		return canAccept(manager.getFinishEvent());
	}

	public void finish() {
		RobotCore robot = getCurrentContext().getRobot();
		if (robot.isTerminated()) {
			log.debug("terminated: {}", robot.getLastState());
			return;
		}
		if (robot.isError()) {
			log.debug("error: {}", robot.getLastState());
			return;
		}
		robot.fire(manager.getFinishEvent(), getCurrentContext().getCommandContext());
	}

	public boolean canFinishWithError() {
		return canAccept(manager.getTerminateEvent());
	}

	public void finishWithError() {
		RobotCore robot = getCurrentContext().getRobot();
		robot.fire(manager.getTerminateEvent(), getCurrentContext().getCommandContext());
	}

	public boolean isMentioned(Message message) {
		if (message.isMentioned(manager.getJda().getSelfUser(), MentionType.USER)) {
			log.trace("mentioned by: {}", manager.getJda().getSelfUser());
			return true;
		}
		return false;
	}

	public String getRootCommand(String message) {
		String msg = StringUtils.trimToEmpty(message).toLowerCase();
		msg = removeStartingSign(msg);
		log.trace("message: {}", msg);

		if (languageResolver.matchesKey(MessageKey.CALL_BOT, msg)) {
			return MessageKey.CALL_BOT;
		}

		return null;
	}

	public String getRootCommand(Message message) {
		return getRootCommand(message.getContentDisplay());
	}

//	public String getRootCommand(Message message) {
//		if (message.isMentioned(jda.getSelfUser(), MentionType.USER)) {
//			log.trace("mentioned by: {}", jda.getSelfUser());
//			return MessageKey.CALL_BOT;
//		}
//
//		String msg = StringUtils.trimToEmpty(message.getContentDisplay()).toLowerCase();
//		msg = removeStartingSign(msg);
//		log.trace("message: {}", msg);
//
//		if (languageResolver.matchesKey(MessageKey.CALL_BOT, msg)) {
//			return MessageKey.CALL_BOT;
//		}
//
//		return null;
//	}

	private String removeStartingSign(String msg) {
		if (msg.startsWith("/") || msg.startsWith("\\") || msg.startsWith("-") || msg.startsWith("@")) {
			return msg.replaceAll("\\W", "");
		}
		return msg;
	}

//	public String greet(User user) {
//		String userName = SafeUtil.get(() -> user.getName(), "");
//		return languageResolver.getMessage(MessageKey.REPLY_GREETING, userName);
//	}

//	@Deprecated
//	public String getSubCommand(Message message, String parentCommand) {
//		if (MessageKey.CALL_BOT.equals(parentCommand)) {
//
//		}
//
//		return null;
//	}

}
