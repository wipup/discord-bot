package wp.discord.bot.delete;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.AudioSendHandler;

public class AudioSender implements AudioSendHandler, AudioLoadResultHandler {

	private static final String test_music = "D:\\temp\\discord_music.mp3";
	private JDA jda;
	private AudioPlayer audioPlayer;
	private AudioFrame lastFrame;

	public AudioSender(JDA jda) {
		super();
		this.jda = jda;
		init();
	}

	protected void init() {
		AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerLocalSource(playerManager);
		
		playerManager.loadItem(test_music, this);
		audioPlayer = playerManager.createPlayer();
	}

	@Override
	public boolean canProvide() {
		lastFrame = audioPlayer.provide();
		return lastFrame != null;
	}

	@Override
	public ByteBuffer provide20MsAudio() {
		return ByteBuffer.wrap(lastFrame.getData());
	}

	@Override
	public boolean isOpus() {
		return true;
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		System.out.println("AudioTracked loaded: " + track.getIdentifier());
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		System.out.println("Playlist loaded");
	}

	@Override
	public void noMatches() {
		System.out.println("Identifier not found");
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		System.out.println("Failed to load identifier");
		exception.printStackTrace();
	}

	public AudioPlayer getAudioPlayer() {
		return audioPlayer;
	}
}
