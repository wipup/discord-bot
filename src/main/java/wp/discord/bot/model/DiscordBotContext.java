package wp.discord.bot.model;

import java.util.Map;
import java.util.Set;

public class DiscordBotContext {

	private Map<DiscordUserRole, Set<DiscordUser>> managedUsers;

	public Map<DiscordUserRole, Set<DiscordUser>> getManagedUsers() {
		return managedUsers;
	}

	public void setManagedUsers(Map<DiscordUserRole, Set<DiscordUser>> managedUsers) {
		this.managedUsers = managedUsers;
	}
}
