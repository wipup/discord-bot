package wp.discord.bot.constant;

import lombok.Getter;

@Getter
public enum CmdToken {

	// discord entity
	CHANNEL("channel", 1), //
	USER("user", 1), //
	AUDIO("audio", 1), //
	MESSAGE("message", 1), //

	// robot entity
	SCHEDULE("schedule", 1), //
	AUTO_REPLY("reply", 1), //
	CRON("cron", 6), // cron

	// feature
	VALUE("value", 100), //
	ACTIVE("active", 1), //
	CMD("cmd", 10), //
	NAME("name", 1), //
	REACTION("reaction", 1), //
	ID("id", 1), //
	COUNT("count", 1), //
	TIME("time", 1), //
	REPEAT("repeat", 1), //
	ADMIN("admin", 1), //
	ALL("all", 2), // //e.g. bot play audio <time-to-xxx> user all 3

	// system
	LOG("log", 1), STATUS("status", 1), //
	ACTIVITY("activity", 2), //
	;

	private String cmd;
	private int parameterCount = 0;

	private CmdToken(String cmd) {
		this(cmd, 0);
	}

	private CmdToken(String cmd, int paramCount) {
		this.cmd = cmd;
		this.parameterCount = paramCount;
	}

	public boolean accept(String command) {
		return cmd.equalsIgnoreCase(command);
	}

	public static CmdToken getMatchingEntity(String cmd) {
		for (CmdToken a : CmdToken.values()) {
			if (a.accept(cmd)) {
				return a;
			}
		}
		return null;
	}
}
