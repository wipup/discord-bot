package wp.discord.bot.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CommandProcessor {

	@Autowired
	private DiscordJDABot bot;

	public void handleCommand(String command) throws Exception {
		for (String fragment : command.split("\\s")) {
			if (StringUtils.isEmpty(fragment)) {
				continue;
			}

			if (bot.canAccept(fragment)) {
				log.debug("accept: {}", fragment);
				bot.fireEvent(fragment);

			} else {
				log.debug("reject: {}", fragment);
				// terminate
			}
			log.debug("--------------------");
		}
	}

}
