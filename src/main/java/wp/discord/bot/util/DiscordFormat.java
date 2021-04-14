package wp.discord.bot.util;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class DiscordFormat {

	public static String mentionEveryone() {
		return "@everyone";
	}

	public static String mention(User user) {
		return mentionUser(user.getId());
	}

	public static String mentionUser(String id) {
		return MessageFormat.format("'<@!'{0}'>'", extractId(id));
	}

	public static String mention(Role role) {
		return mentionRole(role.getId());
	}

	public static String mentionRole(String id) {
		return MessageFormat.format("'<@&'{0}'>'", extractId(id));
	}

	public static String mention(GuildChannel channel) {
		return mentionChannel(channel.getId());
	}

	public static String mentionChannel(String id) {
		return MessageFormat.format("'<#'{0}'>'", extractId(id));
	}

	public static String reaction(String name) {
		return MessageFormat.format("':'{0}':'", name);
	}

	public static String extractId(String mention) {
		return mention.replaceAll("[^\\d]", "").trim();
	}

	public static String code(String message) {
		return MessageFormat.format("`{0}`", message);
	}

	public static String codeBlock(String message) {
		return codeBlock(message, null);
	}

	public static String codeBlock(String message, String language) {
		language = StringUtils.defaultString(language);
		return MessageFormat.format("```{0}\n{1}\n```\n", language, message);
	}

	public static String quote(String message) {
		return ">>> " + message;
	}
}
