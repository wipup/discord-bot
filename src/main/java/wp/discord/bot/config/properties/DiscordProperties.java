package wp.discord.bot.config.properties;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import wp.discord.bot.util.ToStringUtils;

@Data
@ConfigurationProperties(prefix = "discord")
public class DiscordProperties {

	private String oauth2Url;
	private String token;
	private Integer largeThreshold;
	private DiscordStatus status;
	private Set<DiscordUserProperties> users;

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
