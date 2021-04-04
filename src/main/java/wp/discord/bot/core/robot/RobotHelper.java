package wp.discord.bot.core.robot;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import wp.discord.bot.locale.MessageKey;
import wp.discord.bot.locale.MessageLanguageResolver;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class RobotHelper {

	@Autowired
	private MessageLanguageResolver languageResolver;
	
	@Autowired
	private JDA jda;

	public void setGreetAction(User user, MessageChannel channel, Map<String, Object> context) {
		context.put(RobotField.CHANNEL, channel);
	}

	public void setVoiceChannel(VoiceChannel channel, Map<String, Object> context) {
		context.put(RobotField.VOICE_CHANNEL, channel);
		context.put(RobotField.CHANNEL, channel);
	}

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

			VoiceChannel vc = event.getMember().getVoiceState().getChannel();
			return vc;
		} catch (Exception e) {
			log.error("error findAuthorVoiceChannel", e);
		}
		return null;
	}

	public void executeCommand(String from, String to, String event, Map<String, Object> context) {
		try {
			String action = (String) context.get(RobotField.ACTION);

			if (RobotField.ACTION_GREET_AUTHOR.equalsIgnoreCase(action)) {
				greetAuthor(context);
			} else if (RobotField.ACTION_JOIN_VOICE_CHANNEL.equalsIgnoreCase(action)) {
				joinVoiceChannel(context);
			} else if (RobotField.ACTION_LOG_OUT.equalsIgnoreCase(action)) {
				shutdown(context);
			} else {
				
			}
		} catch (Exception e) {
			log.error("error", e);
			// error
		}
	}

	public void greetAuthor(Map<String, Object> context) {
		GenericEvent event = (GenericEvent) context.get(RobotField.JDA_EVENT);

		if (event instanceof MessageReceivedEvent) {
			MessageReceivedEvent e = (MessageReceivedEvent) event;

			String userName = SafeUtil.get(() -> e.getAuthor().getName(), "");
			String greetSentence = languageResolver.getMessage(MessageKey.REPLY_GREETING, userName);
			e.getChannel().sendMessage(greetSentence).queue();
		}

	}

	public void joinVoiceChannel(Map<String, Object> context) {
		VoiceChannel vc = (VoiceChannel) context.get(RobotField.VOICE_CHANNEL);
		if (vc != null) {
			AudioManager audio = vc.getGuild().getAudioManager();
			audio.openAudioConnection(vc);

			return;
		}

		GenericEvent event = (GenericEvent) context.get(RobotField.JDA_EVENT);
		if (event instanceof GenericMessageEvent) {
			GenericMessageEvent msgEvent = (GenericMessageEvent) event;
			String reply = languageResolver.getMessage(MessageKey.REPLY_CHANNEL_REQUIRED);
			msgEvent.getChannel().sendMessage(reply).queue();
		}

	}
	
	public void shutdown(Map<String, Object> context) {
		MessageReceivedEvent event = (MessageReceivedEvent) context.get(RobotField.JDA_EVENT);
		String adminId = ""; // TODO FIXME
		if (event.getAuthor().getId().equals(adminId)) {
			jda.shutdownNow();
		} else {
			// error
		}
	}
}
