package wp.discord.bot.task.add;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class AddActionHandler implements ActionHandler {

	@Autowired
	private AddScheduleTask addAlert;

	@Override
	public void handleAction(BotAction action) throws Exception {
		String targetEntity = SafeUtil.get(() -> action.getActionParams().get(0));
		CmdEntity entity = CmdEntity.getMatchingEntity(targetEntity);
		if (entity == null) {
			Reply rep = Reply.of().literal("Unknown entity: ").code(targetEntity);
			throw new ActionFailException(rep);
		}

		log.debug("adding new : {}", entity);
		if (entity == CmdEntity.SCHEDULE) {
			addAlert.addAlert(action);
			return;
		}

		if (entity == CmdEntity.AUTO_REPLY) {
			// TODO
		}

		Reply rep = Reply.of().literal("Unsupported adding entity: ").code(targetEntity);
		throw new ActionFailException(rep);
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.ADD;
	}

}