package wp.discord.bot.core.action;

import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.model.BotAction;

public interface ActionHandler {

	public void handleAction(BotAction action) throws Exception;

	public CmdAction getAcceptedAction();

}
