package wp.discord.bot.listener.text;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.core.DiscordEventListener;

@Component
@Slf4j
public class GuildTextMessageListener extends DiscordEventListener<MessageReceivedEvent> {

	@Override
	public void handleEvent(MessageReceivedEvent event) throws Exception {
		MessageChannel channel = event.getChannel();
		Message message = event.getMessage();
		String msg = message.getContentDisplay();

		log.info("[{}][{}] {}: {}", event.getGuild().getName(), channel.getName(), event.getAuthor(), msg);
		log.info("           : {}", message.getContentRaw());
	}

	@Override
	public boolean acceptCondition(MessageReceivedEvent event) {
		return event.isFromType(ChannelType.TEXT);
	}

	@Override
	public Class<MessageReceivedEvent> eventClass() {
		return MessageReceivedEvent.class;
	}

}
