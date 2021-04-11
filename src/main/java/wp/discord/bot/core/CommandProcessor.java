package wp.discord.bot.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.core.action.ActionSelector;

@Component
@Slf4j
public class CommandProcessor implements ThreadContextAware {

	@Autowired
	private DiscordJDABot bot;

	@Autowired
	private ActionSelector actionSelector;
	
	public void handleCommand(String command) throws Exception {
		String current = "";

		for (String fragment : command.split("\\s")) {
			if (StringUtils.isEmpty(fragment)) {
				continue;
			}

			current = current + fragment;

			if (bot.canAccept(current)) {
				log.debug("accept: {}", current);
				bot.fireEvent(current);

				current = "";
				log.debug("--------------------");
			} else {
//				log.debug("reject: {}", current);
				// terminate
				current += " ";
			}
		}
		
		if (StringUtils.isNotEmpty(current)) {
			// error
			log.debug("not accept: {}", current);
		} 
		
		actionSelector.executeQueuedActions();
	}

}
