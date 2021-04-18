package wp.discord.bot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import wp.discord.bot.config.properties.DiscordProperties;
import wp.discord.bot.config.properties.DiscordStatus;
import wp.discord.bot.util.SafeUtil;

@Configuration
@EnableConfigurationProperties(DiscordProperties.class)
@Slf4j
public class JDAConfig {

	@Autowired
	private DiscordProperties discordProperties;

	@Bean
	public JDA discordJDA() throws Exception {
		try {
			log.debug("configuration: {}", discordProperties);
			DiscordStatus status = discordProperties.getStatus();

			JDABuilder builder = JDABuilder.createDefault(discordProperties.getToken());
			builder.setActivity(Activity.of(status.getType(), status.getName()));
			builder.setLargeThreshold(SafeUtil.nonNull(() -> discordProperties.getLargeThreshold(), 50));
			builder.setStatus(status.getStatus());

			return builder.build().awaitReady();
		} catch (Exception e) {
			log.error("error: ", e);
			throw e;
		}
	}

}
