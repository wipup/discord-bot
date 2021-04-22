package wp.discord.bot.constant;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

@Getter
public enum CmdAction {

	GREET("hello"), SHUTDOWN("shutdown"), SET("set", 1), //
	HELP("help", 10),

	// ------------------

	JOIN_VOICE_CHANNEL("join"), LEAVE_VOICE_CHANNEL("leave"), PLAY_AUDIO("play"), //

	SEND_MESSAGE_TO_TEXT_CHANNEL("send"), SEND_MESSAGE_TO_PRIVATE_CHANNEL("pm"), //

	// -----------------

	RUN("run", 1), //
	COMPILE_CRON("cron", 6), //
	GET("get", 1), ADD("add", 1, "create"), DELETE("delete", 1, "del"), UPDATE("update", 1), // CRUD
	TRANSFER("transfer", 1), //
	;

	private String cmd;
	private String shortCmd;
	private int parameterCount = 0;

	private CmdAction(String cmd) {
		this(cmd, 0);
	}

	private CmdAction(String cmd, int paramCount, String shortCmd) {
		this.cmd = cmd;
		this.parameterCount = paramCount;
		this.shortCmd = StringUtils.trimToNull(shortCmd);
	}

	private CmdAction(String cmd, int paramCount) {
		this(cmd, paramCount, null);
	}

	public boolean accept(String command) {
		boolean match = cmd.equalsIgnoreCase(command);
		if (!match && shortCmd != null) {
			return shortCmd.equalsIgnoreCase(command);
		}
		return match;
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
