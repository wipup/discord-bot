package wp.discord.bot.listener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GatewayPingEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import wp.discord.bot.core.DiscordEventListener;

//@Component
@Slf4j
public class GenericEventListener extends DiscordEventListener<GenericEvent> {

	@Override
	public void handleEvent(GenericEvent event) throws Exception {
		if (event instanceof GatewayPingEvent) {
			return;
		}

		log.debug("Received Event: {}", event.getClass().getSimpleName());
		log.debug("              : {}", event);
	}

	@Override
	public Class<GenericEvent> eventClass() {
		return GenericEvent.class;
	}

}