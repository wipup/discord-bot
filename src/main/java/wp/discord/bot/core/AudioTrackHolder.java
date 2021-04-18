package wp.discord.bot.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.config.properties.DiscordProperties;
import wp.discord.bot.core.bot.BotSessionManager;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class AudioTrackHolder implements InitializingBean, AudioLoadResultHandler {

	@Autowired
	private DiscordProperties discordProperties;

	@Autowired
	private BotSessionManager botSessionManager;

	private AudioPlayerManager audioPlayerManager;
	private Map<String, AudioTrack> audioTracks; // key = name

	// temporary
	private transient Map<String, String> trackFilePathMap; // key = path

	public AudioTrackHolder() {
		init();
	}

	private void init() {
		audioPlayerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerLocalSource(audioPlayerManager);

		audioTracks = new LinkedHashMap<>();
	}

	public AudioTrack getAudioTrack(String name) {
		AudioTrack track = audioTracks.get(name);
		if (track == null) {
			log.error("audio-tracking not found: {}", name);
		}
		return track;
	}

	public List<AudioTrack> getAllAudioTracks() {
		return audioTracks.values().stream().sorted((a, b) -> a.getUserData().toString().compareTo(b.getUserData().toString())).collect(Collectors.toList());
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

	public AudioPlayerManager getAudioPlayerManager() {
		return audioPlayerManager;
	}

	// ------------------------------------------

	@Override
	public void afterPropertiesSet() throws Exception {
		loadAudioTracks();
	}

	public void reloadAudio() throws Exception {
		init();
		audioTracks.clear();
		loadAudioTracks();
		botSessionManager.getAllSessions().stream().forEach((s) -> {
			botSessionManager.updateBotAudioPlayer(s, audioPlayerManager.createPlayer());
		});
	}

	private void loadAudioTracks() throws Exception {
		trackFilePathMap = new ConcurrentHashMap<>();

		String audioDir = discordProperties.getAudioFolder();
		if (StringUtils.isEmpty(audioDir)) {
			log.warn("Audio path is empty");
			return;
		}
		Path audioPath = Paths.get(audioDir);
		if (!Files.isDirectory(audioPath)) {
			throw new IllegalStateException("Not Directory: Audio path: " + audioPath);
		}

		try (Stream<Path> pathStream = Files.list(audioPath)) {
			pathStream.filter(Files::isRegularFile) //
					.filter(Files::isReadable) //
					.filter((p) -> StringUtils.endsWithIgnoreCase(p.getFileName().toString(), ".mp3")) //
					.map((p) -> p.toAbsolutePath()).forEach((absPath) -> {
						String absolutePath = absPath.toString();
						String fileName = absPath.getFileName().toString();

						trackFilePathMap.put(absolutePath, fileName);
						log.debug("loading: {}", fileName);
						audioPlayerManager.loadItem(absolutePath, this);
					});
		}
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		String path = getAudioTrackFilePath(track);
		log.info("loaded track: {}", path);

		String trackName = trackFilePathMap.get(path);
		if (audioTracks.containsKey(trackName)) {
			throw new IllegalStateException("duplicated track name: " + trackName);
		}

		setAudioTrackName(track, trackName);
		audioTracks.put(trackName, track);
		trackFilePathMap.remove(path);
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
