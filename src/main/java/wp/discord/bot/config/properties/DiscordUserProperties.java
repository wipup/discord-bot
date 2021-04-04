package wp.discord.bot.config.properties;

import wp.discord.bot.model.DiscordUserRole;
import wp.discord.bot.util.ToStringUtils;

public class DiscordUserProperties {

	private String snowflakeId;
	private String alias;
	private DiscordUserRole role;

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

	public String getSnowflakeId() {
		return snowflakeId;
	}

	public void setSnowflakeId(String snowflakeId) {
		this.snowflakeId = snowflakeId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((snowflakeId == null) ? 0 : snowflakeId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiscordUserProperties other = (DiscordUserProperties) obj;
		if (snowflakeId == null) {
			if (other.snowflakeId != null)
				return false;
		} else if (!snowflakeId.equals(other.snowflakeId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
