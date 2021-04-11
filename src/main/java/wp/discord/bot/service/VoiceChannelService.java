package wp.discord.bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import wp.discord.bot.core.action.Action;
import wp.discord.bot.core.action.ActionConstant;
import wp.discord.bot.core.action.ActionExecutorService;
import wp.discord.bot.locale.MessageKey;
import wp.discord.bot.locale.MessageLanguageResolver;
import wp.discord.bot.model.CommandContext;
import wp.discord.bot.service.helper.VoiceChannelHelper;

@Component
@ActionExecutorService
public class VoiceChannelService {

	@Autowired
	private JDA jda;

	@Autowired
	private VoiceChannelHelper helper;

	@Autowired
	private MessageLanguageResolver languageResolver;

	@Action(ActionConstant.ACTION_JOIN_VOICE_CHANNEL)
	public void joinVoiceChannel(CommandContext context) {
		VoiceChannel vc = context.getToVoiceChannel();
		if (vc != null) {
			AudioManager audio = vc.getGuild().getAudioManager();
			audio.openAudioConnection(vc);
			return;

		}

		// if vc == null
		MessageReceivedEvent event = context.getMessageReceivedEvent();
		GenericMessageEvent msgEvent = (GenericMessageEvent) event;
		String reply = languageResolver.getMessage(MessageKey.REPLY_CHANNEL_REQUIRED);
		msgEvent.getChannel().sendMessage(reply).queue();
	}

	@Action(ActionConstant.ACTION_LEAVE_VOICE_CHANNEL)
	public void leaveVoiceChannel(CommandContext context) {
		MessageReceivedEvent event = context.getMessageReceivedEvent();
		String selfId = jda.getSelfUser().getId();
		VoiceChannel vc = helper.findVoiceChannelOfUser(event, selfId);

		if (vc != null) {
			vc.getGuild().getAudioManager().closeAudioConnection();
		}
	}

	@Action(ActionConstant.ACTION_GET_AUTHOR_VOICE_CHANNEL)
	public void getAuthorVoiceChannel(CommandContext context) {
		VoiceChannel vc = helper.findAuthorVoiceChannel((MessageReceivedEvent) context.getJdaEvent());
		context.setAuthorVoiceChannel(vc);
	}

	@Action(ActionConstant.ACTION_JOIN_AUTHOR_VOICE_CHANNEL)
	public void joinAuthorVoiceChannel(CommandContext context) {
		VoiceChannel vc = context.getAuthorVoiceChannel();
		context.setToVoiceChannel(vc);
		joinVoiceChannel(context);
	}
}
