package wp.discord.bot.constant;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;

@Getter
public enum CmdToken {

	// discord entity
	CHANNEL(1, "channel", "ch"), //
	USER(1, "user", "u"), //
	AUDIO(1, "audio", "a"), //
	MESSAGE(1, "message", "msg"), //

	// robot entity
	SCHEDULE(1, "schedule", "sch"), //
	AUTO_REPLY(1, "reply"), //
	CRON(6, "cron"), // cron

	// feature
	VALUE(100, "value", "v"), //
	ACTIVE(1, "active"), //
	CMD(10, "cmd"), //
	NAME(1, "name", "n"), //
	REACTION(1, "reaction"), //
	ID(1, "id", "id"), //
	COUNT(1, "count"), //
	TIME(1, "time", "t"), //
	REPEAT(1, "repeat"), //
	ADMIN(1, "admin", "adm"), //
	ALL(2, "all"), // //e.g. bot play audio <time-to-xxx> user all 3

	// system
	LOG(1, "log"), STATUS(1, "status", "stat"), //
	ACTIVITY(2, "activity", "act"), //
	;

	private Set<String> cmds;
	private int parameterCount = 0;

	private CmdToken(String cmd) {
		this(0, cmd);
	}

	private CmdToken(int paramCount, String... cmds) {
		this.cmds = Arrays.asList(cmds).stream().collect(Collectors.toSet());
		this.parameterCount = paramCount;
	}

	public boolean accept(String command) {
		return cmds.stream().anyMatch((s) -> s.equalsIgnoreCase(command));
	}

	public String getCmd() {
		return cmds.stream().findFirst().get();
	}

	public static CmdToken getMatchingCmdToken(String cmd) {
		for (CmdToken a : CmdToken.values()) {
			if (a.accept(cmd)) {
				return a;
			}
		}
		return null;
	}

}
