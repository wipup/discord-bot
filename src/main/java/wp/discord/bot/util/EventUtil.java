package wp.discord.bot.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;

public class EventUtil {

	public static User getAuthor(GenericEvent event) {
		return SafeUtil.get(() -> (User) event.getClass().getMethod("getAuthor").invoke(event));
	}

	public static Guild getGuild(GenericEvent event) {
		return SafeUtil.get(() -> (Guild) event.getClass().getMethod("getGuild").invoke(event));
	}

	public static MessageChannel getChannel(GenericEvent event) {
		return SafeUtil.get(() -> (MessageChannel) event.getClass().getMethod("getChannel").invoke(event));
	}

	public static String getMessageId(GenericEvent event) {
		return SafeUtil.get(() -> (String) event.getClass().getMethod("getMessageId").invoke(event));
	}
}
