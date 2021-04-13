package wp.discord.bot.core;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import wp.discord.bot.util.SafeUtil;

@Component
public class AudioTrackHandler implements AudioSendHandler {

	@Autowired
	private AudioPlayerManager playerManager;

	private Map<String, AudioTrack> audioTracks;
	private AudioPlayer audioPlayer;
	private AudioFrame lastFrame;

	public void playTrack(AudioTrack track) {
		getAudioPlayer().playTrack(track);
	}

	public AudioTrack getPlayingTrack() {
		return getAudioPlayer().getPlayingTrack();
	}

	public void stopTrack() {
		getAudioPlayer().stopTrack();
	}

	public void addAudioListener(AudioEventListener listener) {
		getAudioPlayer().addListener(listener);
	}

	public void removeAudioListener(AudioEventListener listener) {
		getAudioPlayer().removeListener(listener);
	}

	@Override
	public boolean canProvide() {
		lastFrame = getAudioPlayer().provide();
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

	private AudioPlayer getAudioPlayer() {
		if (audioPlayer == null) {
			audioPlayer = playerManager.createPlayer();
		}
		return audioPlayer;
	}

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
		}
		return track;
	}

	public void setAudioTracks(Map<String, AudioTrack> audioTracks) {
		this.audioTracks = audioTracks;
	}

}
