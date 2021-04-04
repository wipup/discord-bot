package wp.discord.bot.service.text;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.core.DiscordEventListener;
import wp.discord.bot.core.DiscordJDABot;

@Component
@Slf4j
public class CommandMessageListener extends DiscordEventListener<MessageReceivedEvent> {

	@Autowired
	private DiscordJDABot bot;

	@Override
	public void handleEvent(MessageReceivedEvent event) throws Exception {
		String cmd = event.getMessage().getContentDisplay();
		log.debug("[CMD] {}", cmd);

		bot.newRobot(event);
		boolean foundRootCmd = bot.isMentioned(event.getMessage());
		for (String f : cmd.split("\\s")) {
			if (StringUtils.isEmpty(f)) {
				continue;
			}
			if (!foundRootCmd) {
				String rootCmd = bot.getRootCommand(f);
				log.debug("rootCmd: {}", rootCmd);
				if (StringUtils.isNotEmpty(rootCmd)) {
					foundRootCmd = true;
				}
				continue;
			}

			f = f.toLowerCase();
			if (bot.canAccept(f)) {
				log.debug("accept: {}", f);
				bot.fire(f);
			} else {
				log.debug("reject: {}", f);
				// terminate
			}
			log.debug("--------------------");
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
