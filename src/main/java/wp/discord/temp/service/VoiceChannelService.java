package wp.discord.temp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import wp.discord.bot.core.AudioTrackHolder;
import wp.discord.bot.task.helper.VoiceChannelHelper;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.temp.core.action.Action;
import wp.discord.temp.core.action.ActionConstant;
import wp.discord.temp.core.action.ActionExecutor;
import wp.discord.temp.locale.MessageKey;
import wp.discord.temp.model.CommandContext;

@Component
@ActionExecutor
public class VoiceChannelService {

	@Autowired
	private JDA jda;

	@Autowired
	private VoiceChannelHelper helper;

	@Autowired
	private AudioTrackHolder audioService;

	@Action(ActionConstant.ACTION_JOIN_VOICE_CHANNEL)
	public void joinVoiceChannel(CommandContext context) {
		VoiceChannel vc = context.getToVoiceChannel();
		if (vc != null) {
			AudioManager audio = vc.getGuild().getAudioManager();
			audio.openAudioConnection(vc);
//			audio.setSendingHandler(audioService);
			context.setActionDone(true);
			context.setAudioManager(audio);
			return;
		}

		context.setActionDone(false);
		context.setReplyMessageKey(MessageKey.REPLY_CHANNEL_REQUIRED);
	}

	@Action(ActionConstant.ACTION_LEAVE_VOICE_CHANNEL)
	public void leaveVoiceChannel(CommandContext context) {
		VoiceChannel vc = context.getToVoiceChannel();
		if (vc != null) {
			vc.getGuild().getAudioManager().closeAudioConnection();
			context.setActionDone(true);
			return;
		}

		context.setActionDone(false);
		context.setReplyMessageKey(MessageKey.REPLY_CHANNEL_REQUIRED);
	}

	@Action(ActionConstant.ACTION_SET_TO_VOICE_CHANNEL)
	public void setToVoiceChannel(CommandContext context) {
		String channelId = DiscordFormat.extractId(context.getActionValue());
		VoiceChannel vc = jda.getVoiceChannelById(channelId);
		context.setToVoiceChannel(vc);
	}

	@Action(ActionConstant.ACTION_GET_CURRENT_BOT_VOICE_CHANNEL)
	public void getCurrentBotVoiceChannel(CommandContext context) {
		VoiceChannel vc = helper.findVoiceChannelOfUser(jda.getSelfUser()); // not work if user not in voiceChannel

		if (vc == null) {
			vc = helper.findVoiceChannelOfBot();
		}

		context.setToVoiceChannel(vc);
	}

	@Action(ActionConstant.ACTION_JOIN_USER_VOICE_CHANNEL)
	public void joinUserVoiceChannel(CommandContext context) {
		User user = context.getTargetUser();
		if (user == null) {
			context.setActionError(true);
			context.setReplyMessage("Unknown user");
			return;
		}

		VoiceChannel vc = helper.findVoiceChannelOfUser(user);
		if (vc == null) {
			context.setActionError(true);
			context.setReplyMessage("Unknown VoiceChannel");
			return;
		}

		context.setToVoiceChannel(vc);
		joinVoiceChannel(context);
	}

	@Action(ActionConstant.ACTION_GET_AUTHOR_VOICE_CHANNEL)
	public void getAuthorVoiceChannel(CommandContext context) {
		VoiceChannel vc = helper.findAuthorVoiceChannel(context.getMessageReceivedEvent());
		context.setAuthorVoiceChannel(vc);
	}

	@Action(ActionConstant.ACTION_JOIN_AUTHOR_VOICE_CHANNEL)
	public void joinAuthorVoiceChannel(CommandContext context) {
		VoiceChannel vc = context.getAuthorVoiceChannel();
		context.setToVoiceChannel(vc);
		joinVoiceChannel(context);
	}

	@Action(ActionConstant.ACTION_VALIDATE_BOT_IN_AUTHOR_VOICE_CHANNEL)
	public void ensureBotInAuthorVoiceChannel(CommandContext context) {
		getAuthorVoiceChannel(context);
		joinAuthorVoiceChannel(context);
		VoiceChannel vc = context.getAuthorVoiceChannel();

		if (vc == null) {
			context.setActionError(true);
			context.setActionDone(false);
			context.setReplyMessageKey(MessageKey.REPLY_CHANNEL_REQUIRED);
		}
	}

}
