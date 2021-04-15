package wp.discord.bot.listener.text;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.core.AbstractDiscordEventListener;
import wp.discord.bot.core.CommandLineProcessor;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class CommandMessageListener extends AbstractDiscordEventListener<MessageReceivedEvent> {

	@Autowired
	private CommandLineProcessor cmdProcessor;

	@Override
	public void handleEvent(MessageReceivedEvent event) throws Exception {
		try {
			String cmd = event.getMessage().getContentRaw();
			log.info("[CMD] {}", cmd);

			cmdProcessor.handleMultiLineCommand(event, cmd);
		} catch (BotException e) {
			log.error("bot error: {}", e.getMessage(), e);
			
			String reply = SafeUtil.get(() -> e.getReplyMessage().toString());
			if (reply != null) {
				event.getChannel().sendMessage(reply).queue();
			}
		}
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
