package wp.discord.bot.util;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.User;
import wp.discord.bot.constant.Reaction;

@Slf4j
public class Reply {

	protected StringBuilder stringBuilder = new StringBuilder();

	public static Reply of() {
		return new Reply();
	}

	public Reply reaction(Reaction r) {
		stringBuilder.append(r.getCode());
		return this;
	}

	public Reply append(Reply reply) {
		stringBuilder.append(reply.stringBuilder);
		return this;
	}

	public Reply append(String s) {
		stringBuilder.append(s);
		return this;
	}

	public Reply strikethrough(String s) {
		stringBuilder.append(DiscordFormat.strikethrough(s));
		return this;
	}

	public Reply bold(String s) {
		stringBuilder.append(DiscordFormat.bold(s));
		return this;
	}

	public Reply underlined(String s) {
		stringBuilder.append(DiscordFormat.underlined(s));
		return this;
	}

	public Reply italic(String s) {
		stringBuilder.append(DiscordFormat.italic(s));
		return this;
	}

	public Reply spoiler(String s) {
		stringBuilder.append(DiscordFormat.wrapSpoiler(s));
		return this;
	}

	public Reply startSpoiler() {
		stringBuilder.append(DiscordFormat.startSpoiler());
		return this;
	}

	public Reply endSpoiler() {
		stringBuilder.append(DiscordFormat.endSpoiler());
		return this;
	}

	public Reply literal(Number n) {
		return literal(String.valueOf(n));
	}

	public Reply literal(String s) {
		stringBuilder.append(DiscordFormat.literal(s));
		return this;
	}

	public Reply quote(String s) {
		stringBuilder.append(DiscordFormat.quote(s));
		return this;
	}

	public Reply startCodeBlock() {
		return startCodeBlock("");
	}

	public Reply startCodeBlock(String language) {
		stringBuilder.append(DiscordFormat.startCodeBlock(language));
		return this;
	}

	public Reply endCodeBlock() {
		stringBuilder.append(DiscordFormat.endCodeBlock());
		return this;
	}

	public Reply codeBlock(String s, String lang) {
		stringBuilder.append(DiscordFormat.codeBlock(s, lang));
		return this;
	}
	
	public Reply codeBlock(String s) {
		stringBuilder.append(DiscordFormat.codeBlock(s));
		return this;
	}

	public Reply code(Number s) {
		return code(String.valueOf(s));
	}

	public Reply code(String s) {
		stringBuilder.append(DiscordFormat.code(s));
		return this;
	}

	public Reply mention(User user) {
		if (user != null) {
			stringBuilder.append(DiscordFormat.mention(user));
		}
		return this;
	}

	public Reply mentionUser(String userId) {
		if (StringUtils.isNotEmpty(userId)) {
			stringBuilder.append(DiscordFormat.mentionUser(userId));
		}
		return this;
	}

	public Reply mentionChannel(String channelId) {
		if (StringUtils.isNotEmpty(channelId)) {
			stringBuilder.append(DiscordFormat.mentionChannel(channelId));
		}
		return this;
	}

	public Reply mention(GuildChannel channel) {
		if (channel != null) {
			stringBuilder.append(DiscordFormat.mention(channel));
		}
		return this;
	}

	public Reply newline() {
		stringBuilder.append("\n");
		return this;
	}

	public String buildUnquoted() {
		String build = stringBuilder.toString();
		log.debug("Build Reply: \n{}", build);
		return build;
	}

	public String build() {
		return toString();
	}

	@Override
	public String toString() {
		return DiscordFormat.quote(buildUnquoted());
	}
}
