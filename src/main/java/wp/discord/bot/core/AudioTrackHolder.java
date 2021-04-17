package wp.discord.bot.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.config.properties.DiscordAudioProperties;
import wp.discord.bot.config.properties.DiscordProperties;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class AudioTrackHolder implements InitializingBean, AudioLoadResultHandler {

	@Autowired
	private AudioPlayerManager audioPlayerManager;

	@Autowired
	private DiscordProperties discordProperties;

	private Map<String, AudioTrack> audioTracks; // key = name
	private transient Map<String, DiscordAudioProperties> trackProperties; // key = path

	public AudioTrack getAudioTrack(String name) {
		AudioTrack track = audioTracks.get(name);
		if (track == null) {
			log.error("audio-tracking not found: {}", name);
		}
		return track;
	}

	public List<AudioTrack> getAllAudioTracks() {
		return audioTracks.values().stream().collect(Collectors.toList());
	}

	public void setAudioTrackName(AudioTrack track, String name) {
		track.setUserData(name);
	}
	
	public String getAudioTrackName(AudioTrack track) {
		return SafeUtil.get(() -> track.getUserData().toString());
	}

	public String getAudioTrackFilePath(AudioTrack track) {
		return SafeUtil.get(() -> track.getIdentifier());
	}

	// ------------------------------------------

	@Override
	public void afterPropertiesSet() throws Exception {
		loadAudioTracks();
	}

	private void loadAudioTracks() {
		audioTracks = new LinkedHashMap<>();
		trackProperties = new HashMap<>();

		if (CollectionUtils.isEmpty(discordProperties.getAudios())) {
			log.warn("Audio path is empty");
			return;
		}

		for (DiscordAudioProperties audioProperty : discordProperties.getAudios()) {
			String path = audioProperty.getPath();
			trackProperties.put(path, audioProperty);

			log.debug("loading: {}", path);
			audioPlayerManager.loadItem(path, this);
		}
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		String path = getAudioTrackFilePath(track);
		log.info("loaded track: {}", path);

		String trackName = trackProperties.get(path).getName();
		if (audioTracks.containsKey(trackName)) {
			throw new IllegalStateException("duplicated track name: " + trackName);
		}

		setAudioTrackName(track, trackName);
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
