package wp.discord.bot.service.helper;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Component
@Slf4j
public class VoiceChannelHelper {

	public VoiceChannel findVoiceChannelOfUser(MessageReceivedEvent event, User user) {
		return findVoiceChannelOfUser(event, user.getId());
	}

	public VoiceChannel findVoiceChannelOfUser(MessageReceivedEvent event, String id) {
		try {
			List<Guild> guilds = event.getAuthor().getMutualGuilds();
			for (Guild g : guilds) {
				List<VoiceChannel> vcList = g.getVoiceChannels();
				for (VoiceChannel vc : vcList) {
					List<Member> ms = vc.getMembers();
					for (Member member : ms) {
						if (member.getId().equals(id)) {
							return vc;
						}
					}
				}
			}

		} catch (Exception e) {
			log.error("error findVoiceChannelOfUser: {}", id, e);
		}
		return null;
	}

	public VoiceChannel findAuthorVoiceChannel(MessageReceivedEvent event) {
		return findVoiceChannelOfUser(event, event.getAuthor().getId());
	}
}
