package wp.discord.bot.model;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import wp.discord.bot.core.machine.StateDriver;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Setter
public class DiscordBotContext {

	private Map<DiscordUserRole, Set<DiscordUser>> managedUsers;
	private String language;
	private StateDriver driver;
	private CommandContext commandContext;

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
