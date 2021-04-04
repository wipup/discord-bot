package wp.discord.bot.config.properties;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import wp.discord.bot.util.ToStringUtils;

@ConfigurationProperties(prefix = "discord")
public class DiscordProperties {

	private String oauth2Url;
	private String token;
	private Integer largeThreshold;
	private DiscordStatus status;
	private Set<DiscordUserProperties> users;

	public Set<DiscordUserProperties> getUsers() {
		return users;
	}

	public void setUsers(Set<DiscordUserProperties> users) {
		this.users = users;
	}

	public String getOauth2Url() {
		return oauth2Url;
	}

	public void setOauth2Url(String oauth2Url) {
		this.oauth2Url = oauth2Url;
	}

	public Integer getLargeThreshold() {
		return largeThreshold;
	}

	public void setLargeThreshold(Integer largeThreshold) {
		this.largeThreshold = largeThreshold;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public DiscordStatus getStatus() {
		return status;
	}

	public void setStatus(DiscordStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
