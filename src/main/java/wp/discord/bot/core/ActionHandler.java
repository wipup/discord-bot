package wp.discord.bot.core;

import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.model.bot.BotAction;

public interface ActionHandler {

	public void handleAction(BotAction action) throws Exception;

	public CmdAction getAcceptedAction();

}
