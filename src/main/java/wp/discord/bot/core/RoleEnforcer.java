package wp.discord.bot.core;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wp.discord.bot.core.bot.UserManager;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.model.DiscordUserRole;
import wp.discord.bot.util.Reply;

@Component
public class RoleEnforcer {

	@Autowired
	private UserManager userManager;

	public void allowOnlyRole(BotAction action, DiscordUserRole... enforceRoles) throws Exception {
		allowOnlyRole(action, Arrays.asList(enforceRoles));
	}

	public void allowOnlyRole(BotAction action, List<DiscordUserRole> enforceRoles) throws Exception {
		DiscordUserRole userRole = userManager.getRoleOf(action.getAuthorId());
		if (!enforceRoles.contains(userRole)) {
			Reply r = Reply.of().bold("Permission Denied").newline() //
					.literal("Only users with role [") //
					.bold(StringUtils.join(enforceRoles.stream().map(DiscordUserRole::name).collect(Collectors.toList()), ", ")) //
					.literal("] can access this feature.");
			throw new ActionFailException(r);
		}
	}

	public void allowOnlyOwner(BotAction action) throws Exception {
		allowOnlyRole(action, DiscordUserRole.OWNER);
	}

	public void allowOnlyAdminOrHigher(BotAction action) throws Exception {
		allowOnlyRole(action, DiscordUserRole.OWNER, DiscordUserRole.ADMIN);
	}
}
