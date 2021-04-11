package wp.discord.bot.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import wp.discord.bot.core.action.ActionExecutionRoute;
import wp.discord.bot.core.machine.StateDriver;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Setter
public class DiscordBotContext {

	private Map<DiscordUserRole, Set<DiscordUser>> managedUsers;
	private String language;
	private StateDriver driver;
	private CommandContext commandContext;

	private List<ActionExecutionRoute> methodInvokeQueue;
	
	public List<ActionExecutionRoute> getMethodInvokeQueue() {
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
