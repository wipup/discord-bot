package wp.discord.bot.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import wp.discord.bot.config.properties.DiscordAudioProperties;
import wp.discord.bot.config.properties.DiscordProperties;
import wp.discord.bot.config.properties.DiscordStatus;
import wp.discord.bot.core.AbstractDiscordEventListener;
import wp.discord.bot.core.AudioTrackHandler;
import wp.discord.bot.util.SafeUtil;

@Configuration
@EnableConfigurationProperties(DiscordProperties.class)
@Slf4j
public class JDAConfig implements AudioLoadResultHandler {

	@Autowired
	private DiscordProperties discordProperties;

	private Map<String, AudioTrack> audioTracks;
	private DiscordAudioProperties currentLoadedTrack;

	@Bean
	public JDA discordJDA() throws Exception {
		log.debug("configuration: {}", discordProperties);
		DiscordStatus status = discordProperties.getStatus();

		JDABuilder builder = JDABuilder.createDefault(discordProperties.getToken());
		builder.setActivity(Activity.of(status.getType(), status.getName()));
		builder.setLargeThreshold(SafeUtil.nonNull(() -> discordProperties.getLargeThreshold(), 50));
		builder.setStatus(status.getStatus());

//		https://stackoverflow.com/questions/21156599/javas-fork-join-vs-executorservice-when-to-use-which
//		builder.setCallbackPool(null, false) 
//		builder.setEventPool(null, false)

		return builder.build().awaitReady();
	}

	@Autowired
	public void setJdaEventListener(JDA jda, Collection<AbstractDiscordEventListener<?>> eventListeners) {
		log.debug("event listener: {}", eventListeners);
		for (AbstractDiscordEventListener<?> listener : eventListeners) {
			jda.addEventListener(listener);
		}
	}

	@Bean
	public AudioPlayerManager audioPlayerManager() {
		AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerLocalSource(playerManager);
		return playerManager;
	}

	@Autowired
	public void loadAudioTracks(AudioPlayerManager manager, AudioTrackHandler audioService) {
		audioTracks = new LinkedHashMap<>();

		if (CollectionUtils.isEmpty(discordProperties.getAudios())) {
			log.warn("Audio path is empty");
			return;
		}

		for (DiscordAudioProperties a : discordProperties.getAudios()) {
			currentLoadedTrack = a;
			log.debug("loading: {}", currentLoadedTrack);
			manager.loadItem(currentLoadedTrack.getPath(), this);
		}
		audioService.setAudioTracks(audioTracks);
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		log.info("loaded track: {}", track.getIdentifier());

		String trackName = currentLoadedTrack.getName();
		if (audioTracks.containsKey(trackName)) {
			throw new IllegalStateException("duplicated track name: " + trackName);
		}

		track.setUserData(trackName);
		audioTracks.put(trackName, track);
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		log.info("loaded playlist: {}", playlist.getName());
	}

	@Override
	public void noMatches() {
		log.debug("load failed");
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		log.error("error load audio: {}", exception);
		throw exception;
	}

}
