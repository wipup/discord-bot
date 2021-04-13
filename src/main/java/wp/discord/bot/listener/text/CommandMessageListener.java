package wp.discord.bot.listener.text;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.core.CommandProcessor;
import wp.discord.bot.core.AbstractDiscordEventListener;

@Component
@Slf4j
public class CommandMessageListener extends AbstractDiscordEventListener<MessageReceivedEvent> {

	@Autowired
	private CommandProcessor cmdRunner;

	@Override
	public void handleEvent(MessageReceivedEvent event) throws Exception {
		String cmd = event.getMessage().getContentRaw();
		log.debug("[CMD] {}", cmd);

		cmdRunner.handleCommand(cmd);
	}

	@Override
	public boolean acceptCondition(MessageReceivedEvent event) {
		return !event.getAuthor().isBot();
	}

	@Override
	public Class<MessageReceivedEvent> eventClass() {
		return MessageReceivedEvent.class;
	}

}
