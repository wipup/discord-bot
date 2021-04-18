package wp.discord.bot.util;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class DiscordFormat {

	/**
	 * Escape all mark down
	 */
	public static String literal(String text) {
		return text.replaceAll("[\\\\]", "\\\\\\\\") //
				.replaceAll("_", "\\\\_") //
				.replaceAll("[*]", "\\\\*") //
				.replaceAll("[~]", "\\\\~") //
				.replaceAll("[`]", "\\\\`") //
				.replaceAll("[|]", "\\\\|");
	}

	public static String strikethrough(String text) {
		return "~~" + text + "~~";
	}

	public static String bold(String text) {
		return "**" + text + "**";
	}

	public static String underlined(String text) {
		return "__" + text + "__";
	}

	public static String italic(String text) {
		return "*" + text + "*";
	}

	public static String startSpoiler() {
		return "|| ";
	}

	public static String endSpoiler() {
		return " ||";
	}

	public static String wrapSpoiler(String text) {
		return startSpoiler() + text + endSpoiler();
	}

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
		if (StringUtils.isEmpty(mention)) {
			return "";
		}
		return mention.replaceAll("[^\\d]", "").trim();
	}

	public static String code(String message) {
		if (StringUtils.isBlank(message)) {
			message = " - ";
		}
		return MessageFormat.format("`{0}`", message);
	}

	public static String startCodeBlock() {
		return startCodeBlock("");
	}

	public static String startCodeBlock(String language) {
		language = StringUtils.defaultString(language);
		return MessageFormat.format("```{0}\n", language);
	}

	public static String endCodeBlock() {
		return "\n```";
	}

	public static String codeBlock(String message) {
		return codeBlock(message, null);
	}

	public static String codeBlock(String message, String language) {
		return MessageFormat.format("{0}{1}{2}", startCodeBlock(language), message, endCodeBlock());
	}

	public static String quote(String message) {
		return ">>> " + message;
	}
}
