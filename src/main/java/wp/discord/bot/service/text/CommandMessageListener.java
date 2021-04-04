package wp.discord.bot.service.text;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.core.DiscordEventListener;
import wp.discord.bot.core.DiscordJDABot;
import wp.discord.bot.locale.MessageKey;

@Component
@Slf4j
public class CommandMessageListener implements DiscordEventListener<MessageReceivedEvent> {

	@Autowired
	private DiscordJDABot bot;

	@Override
	public void handleEvent(MessageReceivedEvent event) throws Exception {
//		Message message = event.getMessage();

//		String root = bot.getRootCommand(message);
//		if (StringUtils.isEmpty(root)) {
//			return;
//		}

		String cmd = event.getMessage().getContentDisplay();
		log.debug("[CMD] {}", cmd);

		bot.newRobot(event);
		for (String f : cmd.split("\\s")) {
			if (StringUtils.isEmpty(f)) {
				continue;
			}

			f = f.toLowerCase();
			log.debug("canAccept {}? {}", f, bot.canAccept(f));
			if (bot.canAccept(f)) {
				bot.fire(f);
			} else {
				// terminate
			}
		}

		bot.finish();

	}

//	@Deprecated
//	public void handleSubCommand(MessageReceivedEvent event, String subCmd) {
//		switch (subCmd) {
//		case MessageKey.CMD_JOIN:
//			break;
//		default:
//			event.getChannel().sendMessage(bot.greet(event.getAuthor())).queue();
//			break;
//		}
//	}

	@Override
	public boolean acceptCondition(MessageReceivedEvent event) {
		return !event.getAuthor().isBot();
	}

	@Override
	public Class<MessageReceivedEvent> eventClass() {
		return MessageReceivedEvent.class;
	}

}
