package wp.discord.bot.core.action;

import java.util.List;

import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.model.DiscordUserRole;

public interface ActionHandler {

	public void handleAction(BotAction action) throws Exception;

	public CmdAction getAcceptedAction();

	default public List<DiscordUserRole> allowRoles() {
		return DiscordUserRole.ALL_ROLES;
	}
}
