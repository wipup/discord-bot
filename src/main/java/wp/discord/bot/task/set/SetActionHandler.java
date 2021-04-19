package wp.discord.bot.task.set;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.model.DiscordUserRole;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class SetActionHandler implements ActionHandler {

	@Autowired
	private SetBotStatusTask setBotTask;
	
	@Override
	public void handleAction(BotAction action) throws Exception {

		String targetEntity = StringUtils.defaultString(SafeUtil.get(() -> action.getActionParams().get(0)));
		CmdEntity target = CmdEntity.getMatchingEntity(targetEntity);
		if (target == null) {
			Reply reply = Reply.of().literal("Unknown entity: ").code(" " + targetEntity + " ").newline() //
					.literal("");
			throw new ActionFailException(reply);
		}

		if (target == CmdEntity.STATUS) {
			setBotTask.setOnlineStatus(action);
			return;
		}
		if (target == CmdEntity.ACTIVITY) {
			setBotTask.setActivityStatus(action);
			return;
		}

		Reply reply = Reply.of().literal("Unsupported entity: ").code(targetEntity);
		throw new ActionFailException(reply);
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.SET;
	}

	@Override
	public List<DiscordUserRole> allowRoles() {
		return Arrays.asList(DiscordUserRole.OWNER);
	}

}
