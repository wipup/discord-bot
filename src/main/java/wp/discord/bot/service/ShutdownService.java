package wp.discord.bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.JDA;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.core.ActionHandler;
import wp.discord.bot.model.bot.BotAction;

@Component
public class ShutdownService implements ActionHandler {

	@Autowired
	private JDA jda;

	@Override
	public void handleAction(BotAction action) throws Exception {
		jda.shutdownNow();
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.SHUTDOWN;
	}

}
