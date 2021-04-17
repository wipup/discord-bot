package wp.discord.bot.listener.text;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.core.AbstractDiscordEventListener;
import wp.discord.bot.core.CommandLineProcessor;
import wp.discord.bot.core.action.ActionHandleManager;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class CommandMessageListener extends AbstractDiscordEventListener<MessageReceivedEvent> {

	@Autowired
	private CommandLineProcessor cmdProcessor;

	@Autowired
	private ActionHandleManager actionManager;

	@Override
	public void handleEvent(MessageReceivedEvent event) throws Exception {
		try {
			String cmd = event.getMessage().getContentRaw();
			executeCommand(event, cmd);

		} catch (RuntimeException e) {
			Reply reply = Reply.of().literal("Sorry ").mention(event.getAuthor()).literal(", I couldn't understand your request.");
			event.getChannel().sendMessage(reply.build()).queue();
			throw e;
		} catch (BotException e) {
			log.error("bot error: {}", e.getMessage(), e);

			String reply = SafeUtil.get(() -> e.getReplyMessage().toString());
			if (reply != null) {
				event.getChannel().sendMessage(reply).queue();
			} else {
				throw e;
			}
		}
	}

	public void executeCommand(GenericEvent relatedEvent, String cmd) throws Exception {
		if (StringUtils.isBlank(cmd)) {
			return;
		}
		log.info("[CMD] {}", cmd);

		List<BotAction> actions = cmdProcessor.handleMultiLineCommand(relatedEvent, cmd);
		actionManager.executeActions(actions);
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
