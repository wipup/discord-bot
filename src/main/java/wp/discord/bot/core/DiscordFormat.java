package wp.discord.bot.core;

import java.text.MessageFormat;

import net.dv8tion.jda.api.entities.User;

public class DiscordFormat {

	public static String mention(User user) {
		return mention(user.getId());
	}

	public static String mention(String id) {
		return MessageFormat.format("<@!{}>", id);
	}

}
