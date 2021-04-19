package wp.discord.bot.listener.text;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.core.action.ActionHandleManager;
import wp.discord.bot.core.bot.AbstractDiscordEventListener;
import wp.discord.bot.core.cmd.CommandLineProcessor;
import wp.discord.bot.exception.ActionFailException;
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

	@Autowired
	private TracingHandler tracingHandler;

	@Override
	public void handleEvent(MessageReceivedEvent event) throws Exception {
		try {
			String cmd = event.getMessage().getContentRaw();
			executeCommand(event, cmd);

		} catch (RuntimeException e) {
			log.error("Unexpected Runtime Error: {}", e.getMessage(), e);
			handleRuntimeException(event, e);
			throw e;
		} catch (ActionFailException e) {
			log.error("Action Error: {}", e.getMessage(), e); 
			sendReply(event, e);

		} catch (BotException e) {
			log.error("Bot Error: {}", e.getMessage(), e);
			sendReply(event, e);
			throw e;
		}
	}

	private void handleRuntimeException(MessageReceivedEvent event, RuntimeException e) throws Exception {
		Reply reply = Reply.of().literal("Sorry ").mention(event.getAuthor()).literal(", I couldn't understand your request.");
		sendReply(event, new BotException(reply));
	}

	private void sendReply(MessageReceivedEvent event, BotException e) throws Exception {
		String reply = SafeUtil.get(() -> e.getReplyMessage().toString());
		if (reply != null) {
			event.getChannel().sendMessage(reply).queue(tracingHandler.onSendMessageSuccess(), tracingHandler.onSendMessageFail());
		} else {
			throw e;
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
