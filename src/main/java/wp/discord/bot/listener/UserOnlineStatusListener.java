package wp.discord.bot.listener;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import wp.discord.bot.core.AbstractDiscordEventListener;

@Component
public class UserOnlineStatusListener extends AbstractDiscordEventListener<UserUpdateOnlineStatusEvent> {

	@Override
	public void handleEvent(UserUpdateOnlineStatusEvent event) throws Exception {
	
		
	}

	@Override
	public Class<UserUpdateOnlineStatusEvent> eventClass() {
		return UserUpdateOnlineStatusEvent.class;
	}

}
