package wp.discord.bot.constant;

import lombok.Getter;

@Getter
public enum CmdEntity {

	// entity
	CHANNEL("channel", 1), //
	USER("user", 1), //
	AUDIO("audio", 1), //
	MESSAGE("message", 1), //

	// feature
	VALUE("value", 100), //
	SCHEDULE("schedule", 1), //
	AUTO_REPLY("reply", 1), //
	ACTIVE("active", 1), //
	CRON("cron", 6), // cron
	NAME("name", 1), //
	CMD("cmd", 10), //
	REACTION("reaction", 1), //
	ID("id", 1), //
	COUNT("count", 1), //
	ALL("all", 2), // //e.g. bot play audio <time-to-xxx> user all 3

	// system
	STATUS("status", 1), //
	ACTIVITY("activity", 2), //
	;

	private String cmd;
	private int parameterCount = 0;

	private CmdEntity(String cmd) {
		this(cmd, 0);
	}

	private CmdEntity(String cmd, int paramCount) {
		this.cmd = cmd;
		this.parameterCount = paramCount;
	}

	public boolean accept(String command) {
		return cmd.equalsIgnoreCase(command);
	}

	public static CmdEntity getMatchingEntity(String cmd) {
		for (CmdEntity a : CmdEntity.values()) {
			if (a.accept(cmd)) {
				return a;
			}
		}
		return null;
	}
}
