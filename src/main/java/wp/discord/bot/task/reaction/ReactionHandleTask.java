package wp.discord.bot.task.reaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.bot.UserManager;
import wp.discord.bot.core.cmd.EntityReferenceHandler;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.model.Reference;

@Component
@Slf4j
public class ReactionHandleTask {

	@Autowired
	private EntityReferenceHandler refHandler;

	@Autowired
	private UserManager userManager;

	@Autowired
	private CompileCronReactionTask cronReactionTask;

	public void handleAction(GenericMessageReactionEvent event, BotAction action) throws Exception {
		Message message = getMessage(event);
		if (!isThisBotMessage(message) || reactByBot(event)) {
			return;
		}

		Reference ref = refHandler.getReference(message);
		if (ref == null) {
			return;
		}
		log.debug("Reference: {}", ref);
		ReactionEmote emote = event.getReactionEmote();
		log.debug("reaction code: {}, emoji: {}", emote.getAsCodepoints(), emote.getAsReactionCode());

		CmdEntity entity = CmdEntity.getMatchingEntity(ref.getEntity());
		if (entity == CmdEntity.SCHEDULE) {
			return;
		}

		if (entity == CmdEntity.CRON) { // compile cron
			cronReactionTask.handleAction(event, action, message, ref);
			return;
		}

	}

	public Message getMessage(GenericMessageReactionEvent event) {
		String messageId = event.getMessageId();
		return event.getChannel().retrieveMessageById(messageId).complete();
	}

	public boolean isThisBotMessage(Message message) {
		if (message == null) {
			return false;
		}
		return userManager.isThisBot(message.getAuthor());
	}

	public boolean reactByBot(GenericMessageReactionEvent event) {
		return userManager.isThisBot(event.getUserId());
	}

}
