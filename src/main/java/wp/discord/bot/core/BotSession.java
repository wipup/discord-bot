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
		status = BotStatus.VOICE_CHANNEL_IDLE;
	}

	public void leaveVoiceChannel() {
		if (audioManager.isConnected()) {
			audioManager.closeAudioConnection();
			status = BotStatus.NOT_IN_VOICE_CHANNEL;
		}
	}

	// ------------------------------------

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
			status = BotStatus.VOICE_CHANNEL_IDLE;

		} else if (event instanceof TrackStartEvent) {
			status = BotStatus.PLAYING_AUDIO;

		} else if (event instanceof TrackExceptionEvent) {
			stopTrack();
			status = BotStatus.VOICE_CHANNEL_IDLE;
		}
	}

}
