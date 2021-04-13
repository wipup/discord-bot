package wp.discord.bot.model;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import wp.discord.bot.core.action.ActionExecutionInfo;
import wp.discord.bot.core.machine.StateDriver;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Setter
public class DiscordBotContext {

	private String language;
	private StateDriver driver;
	private CommandContext commandContext;

	private List<ActionExecutionInfo> methodInvokeQueue;

	public List<ActionExecutionInfo> getMethodInvokeQueue() {
		if (methodInvokeQueue == null) {
			methodInvokeQueue = new LinkedList<>();
		}
		return methodInvokeQueue;
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
