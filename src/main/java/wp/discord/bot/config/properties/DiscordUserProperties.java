package wp.discord.bot.config.properties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import wp.discord.bot.model.DiscordUserRole;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DiscordUserProperties {

	@EqualsAndHashCode.Include
	private String snowflakeId;
	private String alias;
	private DiscordUserRole role;

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
