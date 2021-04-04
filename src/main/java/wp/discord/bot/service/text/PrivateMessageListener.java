package wp.discord.bot.service.text;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.core.DiscordEventListener;

@Component
@Slf4j
public class PrivateMessageListener extends DiscordEventListener<MessageReceivedEvent> {

	@Override
	public void handleEvent(MessageReceivedEvent event) throws Exception {
		User user = event.getAuthor();
		log.info("[PM] {}: {}", user, event.getMessage().getContentDisplay());
	}

	@Override
	public boolean acceptCondition(MessageReceivedEvent event) {
		return event.isFromType(ChannelType.PRIVATE);
	}

	@Override
	public Class<MessageReceivedEvent> eventClass() {
		return MessageReceivedEvent.class;
	}

}
