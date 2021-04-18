package wp.discord.bot.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "repository")
public class RepositoryProperties {

	private String directoryPath;

}
