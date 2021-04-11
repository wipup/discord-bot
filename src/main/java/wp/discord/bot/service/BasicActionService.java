package wp.discord.bot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import wp.discord.bot.core.action.Action;
import wp.discord.bot.core.action.ActionExecutorService;
import wp.discord.bot.core.action.ActionConstant;
import wp.discord.bot.locale.MessageKey;
import wp.discord.bot.locale.MessageLanguageResolver;
import wp.discord.bot.model.CommandContext;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
@ActionExecutorService
public class BasicActionService {

	@Autowired
	private MessageLanguageResolver languageResolver;

	@Autowired
	private JDA jda;

	@Action(ActionConstant.ACTION_GREET_AUTHOR)
	public void greetAuthor(CommandContext context) {
		GenericEvent event = context.getJdaEvent();

		if (event instanceof MessageReceivedEvent) {
			MessageReceivedEvent e = (MessageReceivedEvent) event;

			String userName = SafeUtil.get(() -> e.getAuthor().getName(), "");
			String greetSentence = languageResolver.getMessage(MessageKey.REPLY_GREETING, userName);
			e.getChannel().sendMessage(greetSentence).queue();
		}
	}

	@Action(ActionConstant.ACTION_JOIN_VOICE_CHANNEL)
	public void joinVoiceChannel(CommandContext context) {
		VoiceChannel vc = context.getVoiceChannel();
		if (vc != null) {
			AudioManager audio = vc.getGuild().getAudioManager();
			audio.openAudioConnection(vc);
			return;

		}

		// vc == null
		GenericEvent event = context.getJdaEvent();
		if (event instanceof GenericMessageEvent) {
			GenericMessageEvent msgEvent = (GenericMessageEvent) event;
			String reply = languageResolver.getMessage(MessageKey.REPLY_CHANNEL_REQUIRED);
			msgEvent.getChannel().sendMessage(reply).queue();
		}

		log.debug("end joinVoiceChannel: {}", context);
	}

	@Action(ActionConstant.ACTION_LOG_OUT)
	public void shutdown(CommandContext context) {
		MessageReceivedEvent event = (MessageReceivedEvent) context.getJdaEvent();
		log.debug("shutting down from user: {}", event.getAuthor());

		String adminId = "743921134944124989"; // TODO FIXME
		if (event.getAuthor().getId().equals(adminId)) {
			jda.shutdownNow();
		} else {
			// error
		}
	}

	// TODO
	public VoiceChannel findAuthorVoiceChannel(MessageReceivedEvent event) {
		try {
			String authorId = event.getAuthor().getId();
			List<Guild> guilds = event.getAuthor().getMutualGuilds();
			for (Guild g : guilds) {
				List<VoiceChannel> vcList = g.getVoiceChannels();
				for (VoiceChannel vc : vcList) {
					List<Member> ms = vc.getMembers();
					for (Member member : ms) {
						if (member.getId().equals(authorId)) {
							return vc;
						}
					}
				}
			}

		} catch (Exception e) {
			log.error("error findAuthorVoiceChannel", e);
		}
		return null;
	}
}
