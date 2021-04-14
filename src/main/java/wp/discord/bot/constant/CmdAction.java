package wp.discord.bot.constant;

import lombok.Getter;

@Getter
public enum CmdAction {

	GREET("hello"), SHUTDOWN("shutdown"), UPDATE_STATUS("update"), //

	// ------------------

	JOIN_VOICE_CHANNEL("join"), LEAVE_VOICE_CHANNEL("leave"), PLAY_AUDIO("play"), //

	SEND_MESSAGE_TO_TEXT_CHANNEL("send"), SEND_MESSAGE_TO_PRIVATE_CHANNEL("pm"), //

	// -----------------

	COMPILE_CRON("cron"), GET("get", 1), ADD("add", 1), DELETE("delete", 1)

	;

	private String cmd;
	private int parameterCount = 0;

	private CmdAction(String cmd) {
		this(cmd, 0);
	}

	private CmdAction(String cmd, int paramCount) {
		this.cmd = cmd;
		this.parameterCount = paramCount;
	}

	public boolean accept(String command) {
		return cmd.equalsIgnoreCase(command);
	}

	public static CmdAction getMatchingAction(String cmd) {
		for (CmdAction cmdAction : CmdAction.values()) {
			if (cmdAction.accept(cmd)) {
				return cmdAction;
			}
		}
		return null;
	}
}