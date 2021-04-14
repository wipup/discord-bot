package wp.discord.bot.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.core.ShutdownHandler;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.model.BotAction;

@Component
public class ShutdownTask implements ActionHandler {

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

}
