package wp.discord.bot.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.GatewayPingEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;
import wp.discord.bot.config.properties.DiscordProperties;
import wp.discord.bot.config.properties.DiscordStatus;
import wp.discord.bot.core.bot.AbstractDiscordEventListener;

@Component
@Slf4j
public class GenericEventListener extends AbstractDiscordEventListener<GenericEvent> {

	@Autowired
	private JDA jda;

	@Autowired
	private DiscordProperties discordProperties;

	@Override
	public void handleEvent(GenericEvent event) throws Exception {
		if (event instanceof GatewayPingEvent) {
			return;
		}
		if (event instanceof HttpRequestEvent) {
			return;
		}

		log.debug("Received Event: {} : {}", event.getClass().getSimpleName(), event);
	}

	@Override
	public Class<GenericEvent> eventClass() {
		return GenericEvent.class;
	}

	@Override
	public void setReady() {
		super.setReady();
		DiscordStatus status = discordProperties.getStatus();
		log.info("Application is ready, setting status: {}", status);

		jda.getPresence().setActivity(Activity.of(status.getType(), status.getName()));
		jda.getPresence().setStatus(status.getStatus());
	}

	@Override
	public boolean isReady() {
		return true;
	}

}
