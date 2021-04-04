package wp.discord.bot.core;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import wp.discord.bot.core.robot.RobotCore;
import wp.discord.bot.core.robot.RobotCoreManager;

@Component
@Slf4j
public class DiscordJDABot implements ThreadContextAware {

//	@Autowired
//	private MessageLanguageResolver languageResolver;

//	@Autowired
//	private JDA jda;

	@Autowired
	private RobotCoreManager manager;

	public <T extends GenericEvent> void newRobot(T event) {
		RobotCore robot = manager.newRobot(event);
		setCurrentRobot(robot);
		setCurrentCommandContext(new HashMap<>());
	}

	public boolean canAccept(String event) {
		RobotCore robot = getCurrentRobot();
		return robot.canAccept(event);
	}

	public void fire(String event) {
		RobotCore robot = getCurrentRobot();
		robot.fire(event, getCurrentCommandContext());
	}

	public boolean canFinish() {
		return canAccept(manager.getFinishEvent());
	}

	public void finish() {
		RobotCore robot = getCurrentRobot();
		robot.fire(manager.getFinishEvent(), getCurrentCommandContext());
	}

	public boolean canFinishWithError() {
		return canAccept(manager.getTerminateEvent());
	}

	public void finishWithError() {
		RobotCore robot = getCurrentRobot();
		robot.fire(manager.getTerminateEvent(), getCurrentCommandContext());
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

//	private String removeStartingSign(String msg) {
//		if (msg.startsWith("/") || msg.startsWith("\\") || msg.startsWith("-") || msg.startsWith("@")) {
//			return msg.replaceAll("\\W", "");
//		}
//		return msg;
//	}

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
