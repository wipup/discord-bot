package wp.discord.bot.config;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import wp.discord.bot.config.properties.DiscordProperties;
import wp.discord.bot.config.properties.DiscordStatus;
import wp.discord.bot.core.DiscordEventListener;
import wp.discord.bot.util.SafeUtil;

@Configuration
@EnableConfigurationProperties(DiscordProperties.class)
@Slf4j
public class JDAConfig {

	@Autowired
	private DiscordProperties discordProperties;

	@Bean
	public JDA discordJDA(Collection<DiscordEventListener<?>> eventListeners) throws Exception {
		log.debug("configuration: {}", discordProperties);
		log.debug("event listener: {}", eventListeners);
		DiscordStatus status = discordProperties.getStatus();

		JDABuilder builder = JDABuilder.createDefault(discordProperties.getToken());
		builder.setActivity(Activity.of(status.getType(), status.getName()));
		builder.setLargeThreshold(SafeUtil.nonNull(() -> discordProperties.getLargeThreshold(), 50));
		builder.setStatus(status.getStatus());

//		https://stackoverflow.com/questions/21156599/javas-fork-join-vs-executorservice-when-to-use-which
//		builder.setCallbackPool(null, false) 
//		builder.setEventPool(null, false)

		eventListeners.stream().forEach(builder::addEventListeners);
		return builder.build().awaitReady();
	}

	@Bean
	public AudioPlayerManager audioPlayerManager() {
		AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerLocalSource(playerManager);
		return playerManager;
	}

}
