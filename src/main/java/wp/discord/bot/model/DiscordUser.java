package wp.discord.bot.model;

import net.dv8tion.jda.api.entities.User;
import wp.discord.bot.util.ToStringUtils;

public class DiscordUser {

	private String alias;
	private DiscordUserRole role;
	private User user;

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public DiscordUserRole getRole() {
		return role;
	}

	public void setRole(DiscordUserRole role) {
		this.role = role;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
