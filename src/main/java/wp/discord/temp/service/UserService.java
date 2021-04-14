package wp.discord.temp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import wp.discord.bot.core.UserRoleManager;
import wp.discord.bot.model.DiscordUserRole;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.temp.core.action.Action;
import wp.discord.temp.core.action.ActionConstant;
import wp.discord.temp.core.action.ActionExecutor;
import wp.discord.temp.model.CommandContext;

@Component
@Slf4j
@ActionExecutor
public class UserService {

	@Autowired
	private JDA jda;
	
	@Autowired
	private UserRoleManager userManager;

	@Action(ActionConstant.ACTION_SET_TARGET_USER)
	public void setTargetUser(CommandContext context) {
		String id = DiscordFormat.extractId(context.getActionValue());
		User user = jda.retrieveUserById(id).complete();
		if (user == null) {
			context.setActionError(true);
			context.setReplyMessage("Invalid User");
			return;
		}
		
		context.setTargetUser(user);
	}

	@Action(ActionConstant.ACTION_ALLOW_ONLY_OWNER)
	public void allowOnlyOwner(CommandContext context) {
		User user = context.getAuthor();
		DiscordUserRole role = userManager.getRoleOf(user);

		if (role != DiscordUserRole.OWNER) {
			log.debug("not allow non-owner user: {}", user);
			context.setReplyMessage("Access Denied");
			context.setActionError(true);
		}
	}

	@Action(ActionConstant.ACTION_ALLOW_ADMINS_AND_OWNER)
	public void allowAdminsAndOwner(CommandContext context) {
		User user = context.getAuthor();
		DiscordUserRole role = userManager.getRoleOf(user);

		if (role != DiscordUserRole.ADMIN && role != DiscordUserRole.OWNER) {
			log.debug("not allow non-admin user: {}", user);
			context.setReplyMessage("Access Denied");
			context.setActionError(true);
		}
	}

	@Action(ActionConstant.ACTION_ALLOW_ONLY_USER_ID)
	public void allowOnlyUserId(CommandContext context) {
		String id = DiscordFormat.extractId(context.getActionValue());
		User user = context.getAuthor();

		if (!user.getId().equals(id)) {
			context.setReplyMessage("Access Denied");
			context.setActionError(true);
		}
	}
}
