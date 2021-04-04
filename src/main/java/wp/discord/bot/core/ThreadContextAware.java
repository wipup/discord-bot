package wp.discord.bot.core;

import java.util.Map;

import wp.discord.bot.core.robot.RobotCore;

public interface ThreadContextAware {

	public static final ThreadLocal<String> LANGUAGE = new ThreadLocal<>();

	public static final ThreadLocal<RobotCore> ROBOT_CORE = new ThreadLocal<>();
	public static final ThreadLocal<Map<String, Object>> CMD_CONTEXT = new ThreadLocal<>();

	default public RobotCore getCurrentRobot() {
		return ROBOT_CORE.get();
	}

	default public void setCurrentRobot(RobotCore ctx) {
		ROBOT_CORE.set(ctx);
	}

	default public void clearCurrentRobot() {
		ROBOT_CORE.remove();
	}

	default public Map<String, Object> getCurrentCommandContext() {
		return CMD_CONTEXT.get();
	}

	default public void setCurrentCommandContext(Map<String, Object> ctx) {
		CMD_CONTEXT.set(ctx);
	}

	default public void clearCurrentCommandContext() {
		CMD_CONTEXT.remove();
	}

	default public String getCurrentLanguage() {
		return LANGUAGE.get();
	}

	default public void setCurrentLanguage(String lang) {
		LANGUAGE.set(lang);
	}

	default public void clearCurrentLanguage() {
		LANGUAGE.remove();
	}

	default public void clearAllThreadContext() {
		clearCurrentCommandContext();
		clearCurrentLanguage();
		clearCurrentRobot();
	}
}
