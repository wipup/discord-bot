package wp.discord.bot.listener.reaction;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import wp.discord.bot.core.AbstractDiscordEventListener;

@Component
@Slf4j
public class ReactionRemoveListener extends AbstractDiscordEventListener<MessageReactionRemoveEvent> {

	@Override
	public void handleEvent(MessageReactionRemoveEvent event) throws Exception {
		String msgId = event.getMessageId();
		
		log.info("ReactionRemove: {} : {}, {}", event, event.getReaction(), event.getReactionEmote());
		
		log.debug("msgId: {}", msgId);
	}

	@Override
	public Class<MessageReactionRemoveEvent> eventClass() {
		return MessageReactionRemoveEvent.class;
	}

}
