package wp.discord.bot.listener.text;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.core.bot.AbstractDiscordEventListener;

@Component
@Slf4j
public class PrivateMessageListener extends AbstractDiscordEventListener<MessageReceivedEvent> {

	@Override
	public void handleEvent(MessageReceivedEvent event) throws Exception {
		User user = event.getAuthor();
		log.info("[PM] {}: {}", user, event.getMessage().getContentRaw());
		
	}

	@Override
	public boolean acceptCondition(MessageReceivedEvent event) {
		return event.isFromType(ChannelType.PRIVATE) && !event.getAuthor().isBot();
	}

	@Override
	public Class<MessageReceivedEvent> eventClass() {
		return MessageReceivedEvent.class;
	}

}
