package wp.discord.bot.task.helper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

@Component
@Slf4j
public class VoiceChannelHelper {

	@Autowired
	private JDA jda;

	// no one used
	@Deprecated
	public VoiceChannel findVoiceChannelOfBot() {
		for (AudioManager am : jda.getAudioManagerCache()) {
			if (am.getConnectedChannel() != null) {
				return am.getConnectedChannel();
			}
		}

		return null;
	}

	public VoiceChannel findVoiceChannelOfUser(User user) {
		String id = user.getId();
		VoiceChannel result = null;
		try {
			List<Guild> guilds = user.getMutualGuilds(); // << not work if user is not in any voiceChannel
			for (Guild g : guilds) {

				List<VoiceChannel> vcList = g.getVoiceChannels();
				for (VoiceChannel vc : vcList) {

					List<Member> ms = vc.getMembers();
					for (Member member : ms) {

						if (member.getId().equals(id)) {
							result = vc;
							return vc;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("error findVoiceChannelOfUser: {}", user, e);
		} finally {
			log.debug("findVoiceChannelOfUser: {}, return: {}", user, result);
		}
		return null;
	}

	// no one used
	@Deprecated
	public VoiceChannel findAuthorVoiceChannel(MessageReceivedEvent event) {
		return findVoiceChannelOfUser(event.getAuthor());
	}
}
