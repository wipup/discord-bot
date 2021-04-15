package wp.discord.bot.util;

import org.apache.commons.lang3.StringUtils;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.User;

public class Reply {

	protected StringBuilder stringBuilder = new StringBuilder();

	public static Reply of() {
		return new Reply();
	}

	public Reply literal(String s) {
		stringBuilder.append(s);
		return this;
	}

	public Reply quote(String s) {
		stringBuilder.append(DiscordFormat.quote(s));
		return this;
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

	public String build() {
		return toString();
	}

	@Override
	public String toString() {
		return DiscordFormat.quote(stringBuilder.toString());
	}
}
