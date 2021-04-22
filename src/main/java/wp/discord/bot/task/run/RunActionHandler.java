package wp.discord.bot.task.run;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.model.DiscordUserRole;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class RunActionHandler implements ActionHandler {

	@Autowired
	private RunScheduleTask runScheduleTask;

	@Override
	public void handleAction(BotAction action) throws Exception {
		String targetEntity = StringUtils.defaultString(SafeUtil.get(() -> action.getActionParams().get(0)));
		CmdToken target = CmdToken.getMatchingCmdToken(targetEntity);
		if (target == null) {
			Reply reply = Reply.of().literal("Unknown entity: ").code(targetEntity);
			throw new ActionFailException(reply);
		}

		if (target == CmdToken.SCHEDULE) {
			runScheduleTask.runSchedule(action);
			return;
		}

		Reply reply = Reply.of().literal("Unsupported entity: ").code(targetEntity);
		throw new ActionFailException(reply);
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.RUN;
	}

	@Override
	public List<DiscordUserRole> allowRoles() {
		return Collections.singletonList(DiscordUserRole.OWNER);
	}
}
