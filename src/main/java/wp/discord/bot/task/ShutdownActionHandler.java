package wp.discord.bot.task;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.core.ShutdownHandler;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.model.DiscordUserRole;

@Component
public class ShutdownActionHandler implements ActionHandler {

	@Autowired
	private ShutdownHandler shutdownHandler;

	@Override
	public void handleAction(BotAction action) throws Exception {
		shutdownHandler.destroy();
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.SHUTDOWN;
	}

	@Override
	public List<DiscordUserRole> allowRoles() {
		return Collections.singletonList(DiscordUserRole.OWNER);
	}
}
