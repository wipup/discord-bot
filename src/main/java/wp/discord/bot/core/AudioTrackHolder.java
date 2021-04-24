package wp.discord.bot.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
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

	private static String[] SUPPORTED_FORMAT = new String[] { //
			".mp3", ".flac", ".wav", ".aac", "mp4", "m4a", "ogg", "flac" //
	};

	@Autowired
	private DiscordProperties discordProperties;

	@Autowired
	private BotSessionManager botSessionManager;

	private AudioPlayerManager audioPlayerManager;
	private Map<String, AudioTrack> audioTracks; // key = name
	private List<AudioTrack> allAudioTracks;

	// temporary
	private transient Map<String, String> trackFilePathMap; // key = path

	public AudioTrackHolder() {
		init();
	}

	private void init() {
		audioPlayerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerLocalSource(audioPlayerManager);

		audioTracks = new LinkedHashMap<>();
		allAudioTracks = new ArrayList<>();
	}

	public AudioTrack getAudioTrack(String name) {
		AudioTrack track = audioTracks.get(name);
		if (track == null) {
			log.error("audio-tracking not found: {}", name);
		}
		return track;
	}

	public List<AudioTrack> getAllAudioTracks() {
		return allAudioTracks;
	}

	public void setAudioTrackName(AudioTrack track, String name) {
		track.setUserData(name);
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

	public List<Future<Void>> reloadAudio() throws Exception {
		init();
		audioTracks.clear();
		allAudioTracks.clear();
		List<Future<Void>> futures = loadAudioTracks();
		botSessionManager.getAllSessions().stream().forEach((s) -> {
			botSessionManager.updateBotAudioPlayer(s, audioPlayerManager.createPlayer());
		});
		return futures;
	}

	private List<Future<Void>> loadAudioTracks() throws Exception {
		trackFilePathMap = new ConcurrentHashMap<>();
		List<Future<Void>> futureList = new LinkedList<>();

		String audioDir = discordProperties.getAudioFolder();
		if (StringUtils.isEmpty(audioDir)) {
			log.warn("Audio path is empty");
			return futureList;
		}
		Path audioPath = Paths.get(audioDir);
		if (!Files.isDirectory(audioPath)) {
			throw new IllegalStateException("Not Directory: Audio path: " + audioPath);
		}

		try (Stream<Path> pathStream = Files.list(audioPath)) {
			pathStream.filter(Files::isRegularFile) //
					.filter(Files::isReadable) //
					.filter((p) -> StringUtils.endsWithAny(p.getFileName().toString().toLowerCase(), SUPPORTED_FORMAT)) //
					.map((p) -> p.toAbsolutePath()).forEach((absPath) -> {
						String absolutePath = absPath.toString();
						String fileName = absPath.getFileName().toString();

						trackFilePathMap.put(absolutePath, fileName);
						log.debug("loading: {}", fileName);

						Future<Void> future = audioPlayerManager.loadItem(absolutePath, this); // this is asynchronous loading
						futureList.add(future);
					});
		}
		return futureList;
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
		allAudioTracks.add(track);
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
