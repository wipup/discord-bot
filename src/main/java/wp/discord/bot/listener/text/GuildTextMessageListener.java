package wp.discord.bot.listener.text;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.core.bot.AbstractDiscordEventListener;

@Component
@Slf4j
public class GuildTextMessageListener extends AbstractDiscordEventListener<MessageReceivedEvent> {

	@Override
	public void handleEvent(MessageReceivedEvent event) throws Exception {
		MessageChannel channel = event.getChannel();
		Message message = event.getMessage();
		String msg = message.getContentRaw();

		log.info("[{}][{}] {}: {}", event.getGuild().getName(), channel.getName(), event.getAuthor(), msg);
	}

	@Override
	public boolean acceptCondition(MessageReceivedEvent event) {
		return event.isFromType(ChannelType.TEXT) && !event.getAuthor().isBot();
	}

	@Override
	public Class<MessageReceivedEvent> eventClass() {
		return MessageReceivedEvent.class;
	}

}
