package wp.discord.bot.core;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import lombok.Data;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import wp.discord.bot.constant.BotStatus;

@Data
public class BotSession implements AudioSendHandler, AudioEventListener {

	private String guildId;
	private Guild guild;
	private BotStatus status;
	private AudioManager audioManager;

	// audio
	private AudioFrame lastFrame;
	private AudioPlayer audioPlayer;

	public void playTrackAndLeaveChannel(AudioTrack track) {
		playTrack(track, (event) -> {
			getAudioPlayer().removeListener(this);
			leaveVoiceChannel();
		});
	}

	public void playTrack(AudioTrack track, AudioEventListener listener) {
		getAudioPlayer().addListener(listener);
		playTrack(track);
	}

	public void playTrack(AudioTrack track) {
		getAudioPlayer().playTrack(track);
	}

	public AudioTrack getPlayingTrack() {
		return getAudioPlayer().getPlayingTrack();
	}

	public void stopTrack() {
		getAudioPlayer().stopTrack();
	}

	public VoiceChannel getConnectedVoiceChannel() {
		return audioManager.getConnectedChannel();
	}

	public void joinVoiceChannel(VoiceChannel vc) {
		audioManager.openAudioConnection(vc);
		setStatus(BotStatus.VOICE_CHANNEL_IDLE);
	}

	public void leaveVoiceChannel() {
		if (audioManager.isConnected()) {
			audioManager.closeAudioConnection();
			setStatus(BotStatus.NOT_IN_VOICE_CHANNEL);
		}
	}

	// ------------------------------------

	public BotStatus getStatus() {
		synchronized (this) {
			return this.status;
		}
	}

	public void setStatus(BotStatus status) {
		synchronized (this) {
			this.status = status;
		}
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

	@Override
	public void onEvent(AudioEvent event) {
		if (event instanceof TrackEndEvent) {
			setStatus(BotStatus.VOICE_CHANNEL_IDLE);

		} else if (event instanceof TrackStartEvent) {
			setStatus(BotStatus.PLAYING_AUDIO);

		} else if (event instanceof TrackExceptionEvent) {
			stopTrack();
			setStatus(BotStatus.VOICE_CHANNEL_IDLE);
		}
	}

}
