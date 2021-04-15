package wp.discord.bot.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import wp.discord.bot.core.bot.UserManager;
import wp.discord.bot.util.EventUtil;
import wp.discord.bot.util.Reply;

@Component
@Slf4j
public class EventErrorHandler {

	@Autowired
	private UserManager userManager;

	public void handleEventError(GenericEvent event, Throwable e) {
		log.error("event error: ", e);
		notifyOwner(event, e);
	}

	public void notifyOwner(GenericEvent event, Throwable e) {
		Reply rep = createReply(event, e);
		if (event != null) {
			rep.newline().literal("Event: ").code(String.valueOf(event));

			User author = EventUtil.getAuthor(event);
			if (author != null) {
				rep.newline().literal("\tUser: ").code(author.toString());
			}

			MessageChannel channel = EventUtil.getChannel(event);
			if (channel != null) {
				rep.newline().literal("\tChannel [").code(channel.getType().name()).literal("]: ").code(channel.toString());
			}

			Guild guild = EventUtil.getGuild(event);
			if (guild != null) {
				rep.newline().literal("\tGuild: ").code(guild.toString());
			}

			String messageId = EventUtil.getMessageId(event);
			if (StringUtils.isNotEmpty(messageId)) {
				rep.newline().literal("\tMessageId: ").code(messageId);
			}
		}

		userManager.getOwnerUser().getUser().openPrivateChannel().complete().sendMessage(rep.toString()).queue();
	}

	public Reply createReply(GenericEvent event, Throwable e) {
		Throwable rootCause = ExceptionUtils.getRootCause(e);
		Reply rep = Reply.of().literal("Error detected: ").code(e.getClass().getName()).literal(" : ").code(e.getMessage()); //
		if (rootCause != null && rootCause != e) {
			rep.newline().literal("\tCaused by: ").code(e.getClass().getName()).literal(" : ").code(e.getMessage()); //
		}
		return rep;
	}

}
