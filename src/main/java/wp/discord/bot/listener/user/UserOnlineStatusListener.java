package wp.discord.bot.listener.user;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import wp.discord.bot.core.AbstractDiscordEventListener;

@Component
@Slf4j
public class UserOnlineStatusListener extends AbstractDiscordEventListener<UserUpdateOnlineStatusEvent> {

	@Override
	public void handleEvent(UserUpdateOnlineStatusEvent event) throws Exception {
		log.debug("User Status Update: {}", event);
	}

	@Override
	public Class<UserUpdateOnlineStatusEvent> eventClass() {
		return UserUpdateOnlineStatusEvent.class;
	}

}
