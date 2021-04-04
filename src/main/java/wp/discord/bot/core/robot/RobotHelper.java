package wp.discord.bot.core.robot;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.model.CommandContext;

@Component
@Slf4j
public class RobotHelper implements RobotActionTarget {

	@Autowired
	private RobotActionRouter actionRouter;

	@Autowired
	private JDA jda;

	public void setGreetAction(User user, MessageChannel channel, CommandContext context) {
		context.setMessageChannel(channel);
		context.setAction(RobotAction.ACTION_GREET_AUTHOR);
	}

	public void setVoiceChannel(VoiceChannel channel, CommandContext context) {
		context.setVoiceChannel(channel);
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

		} catch (Exception e) {
			log.error("error findAuthorVoiceChannel", e);
		}
		return null;
	}

	public void executeCommand(String from, String to, String event, CommandContext context) {
		try {
			String action = context.getAction();
			actionRouter.executeAction(action, context);
		} catch (Exception e) {
			log.error("error", e);
		}
	}

	public JDA getJda() {
		return jda;
	}
}
