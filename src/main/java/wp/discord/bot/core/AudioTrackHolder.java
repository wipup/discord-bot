package wp.discord.bot.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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

	private DiscordAudioProperties currentLoadedTrack;
	private Map<String, AudioTrack> audioTracks;

	public String getAudioTrackName(AudioTrack track) {
		Entry<String, AudioTrack> found = audioTracks.entrySet().stream() //
				.filter((t) -> t.getValue().getIdentifier().equals(track.getIdentifier())) //
				.findFirst().orElse(null);
		return SafeUtil.get(() -> found.getKey());
	}

	public AudioTrack getAudioTrack(String name) {
		AudioTrack track = audioTracks.get(name);
		if (track != null) {
			track = track.makeClone();
		} else {
			log.error("audio-tracking not found: {}", name);
		}
		return track;
	}

	// ------------------------------------------

	@Override
	public void afterPropertiesSet() throws Exception {
		loadAudioTracks();
	}

	private void loadAudioTracks() {
		audioTracks = new LinkedHashMap<>();

		if (CollectionUtils.isEmpty(discordProperties.getAudios())) {
			log.warn("Audio path is empty");
			return;
		}

		for (DiscordAudioProperties a : discordProperties.getAudios()) {
			currentLoadedTrack = a;
			log.debug("loading: {}", currentLoadedTrack);
			audioPlayerManager.loadItem(currentLoadedTrack.getPath(), this);
		}
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
