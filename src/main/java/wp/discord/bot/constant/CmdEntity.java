package wp.discord.bot.constant;

import lombok.Getter;

@Getter
public enum CmdEntity {

	// entity
	CHANNEL("channel", 1), //
	USER("user", 1), //
	AUDIO("audio", 1), //

	MESSAGE("message", 1), //
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
