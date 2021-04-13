package wp.discord.bot.listener.reaction;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import wp.discord.bot.core.AbstractDiscordEventListener;

@Component
@Slf4j
public class ReactionAddListener extends AbstractDiscordEventListener<MessageReactionAddEvent> {

	@Override
	public void handleEvent(MessageReactionAddEvent event) throws Exception {
		String msgId = event.getMessageId();

		log.info("ReactionAdd: {} : {}, {}", event, event.getReaction(), event.getReactionEmote());

		log.debug("msgId: {}", msgId);

		if (event.isFromType(ChannelType.PRIVATE)) {
			Message m = event.getChannel().retrieveMessageById(msgId).submit(false).get();
			log.info("[PM] {}: {}", m.getAuthor(), m.getContentRaw());

		}
	}

	@Override
	public Class<MessageReactionAddEvent> eventClass() {
		return MessageReactionAddEvent.class;
	}

}
