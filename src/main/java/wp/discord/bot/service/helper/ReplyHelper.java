package wp.discord.bot.service.helper;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.User;
import wp.discord.bot.util.Reply;

@Component
public class ReplyHelper {

	public Reply reply() {
		return new Reply();
	}

	public Reply literal(String s) {
		return reply().literal(s);
	}

	public Reply code(String s) {
		return reply().code(s);
	}

	public Reply mention(User user) {
		return reply().mention(user);
	}

	public Reply mention(GuildChannel channel) {
		return reply().mention(channel);
	}

	public Reply newline() {
		return reply().newline();
	}

}
