package wp.discord.bot.task.delete;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class DeleteTask implements ActionHandler {

	@Autowired
	private DeleteScheduleTask deleteScheduleTask;

	@Autowired
	private DeleteMessageTask deleteMessageTask;
	
	@Override
	public void handleAction(BotAction action) throws Exception {
		String targetEntity = SafeUtil.get(() -> action.getActionParams().get(0));
		CmdEntity entity = CmdEntity.getMatchingEntity(targetEntity);
		if (entity == null) {
			Reply rep = Reply.of().literal("Unknown entity: ").code(targetEntity);
			throw new BotException(rep);
		}

		if (entity == CmdEntity.SCHEDULE) {
			deleteScheduleTask.deleteSchedule(action);
			return;
		}
		
		
		if (entity == CmdEntity.MESSAGE) {
			deleteMessageTask.deleteBotMessage(action);
			return;
		}
		
		Reply rep = Reply.of().literal("Unsupported entity: ").code(targetEntity);
		throw new BotException(rep);
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.DELETE;
	}

}
