package wp.discord.bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import wp.discord.bot.core.AudioTrackHandler;
import wp.discord.bot.core.action.Action;
import wp.discord.bot.core.action.ActionConstant;
import wp.discord.bot.core.action.ActionExecutor;
import wp.discord.bot.model.CommandContext;
import wp.discord.bot.service.helper.AudioTrackEndListener;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.SafeUtil;

@Component
@ActionExecutor
@Slf4j
public class AudioService {

	@Autowired
	private AudioTrackHandler audioService;

	@Autowired
	private VoiceChannelService voiceChannelService;

	@Action(ActionConstant.ACTION_LEAVE_VOICE_CHANNEL_AFTER_AUDIO_END)
	public void leaveChannelWhenFinishPlayingAudio(CommandContext context) {

		final AudioEventListener listener = new AudioTrackEndListener() {
			@Override
			public void onTrackEnd(TrackEndEvent event) {
				log.debug("track-end: {}", event.track.getUserData());
				audioService.removeAudioListener(this);
				voiceChannelService.getCurrentBotVoiceChannel(context);
				voiceChannelService.leaveVoiceChannel(context);
			}
		};
	
		audioService.addAudioListener(listener);
	}

	@Action(ActionConstant.ACTION_PLAY_AUDIO_IN_VOICE_CHANNEL)
	public void playAudio(CommandContext context) {
		VoiceChannel vc = context.getToVoiceChannel();
		AudioManager audioManager = context.getAudioManager();
		if (vc == null || audioManager == null) {
			String reply = DiscordFormat.quote("Not in voice channel");
			context.setReplyMessage(reply);
			context.stopCallNextAction();
			return;
		}

		String trackName = context.getActionValue();
		AudioTrack track = audioService.getAudioTrack(trackName);
		if (track == null) {
			String reply = DiscordFormat.quote("Not found  " + DiscordFormat.code(trackName));
			context.setReplyMessage(reply);
			return;
		}

		audioService.playTrack(track);
		String reply = DiscordFormat.quote("Now playing  " + DiscordFormat.code(trackName));
		context.setReplyMessage(reply);
		context.setActionDone(true);

	}

	@Action(ActionConstant.ACTION_STOP_AUDIO_IN_VOICE_CHANNEL)
	public void stopAudio(CommandContext context) {
		AudioTrack track = SafeUtil.get(() -> audioService.getPlayingTrack());
		audioService.stopTrack();

		String trackName = audioService.getAudioTrackName(track);
		if (trackName != null) {
			String reply = DiscordFormat.quote("Stop playing  " + DiscordFormat.code(trackName));
			context.setReplyMessage(reply);

		} else {
			String reply = DiscordFormat.quote("Audio stopped");
			context.setReplyMessage(reply);
		}
	}

}
