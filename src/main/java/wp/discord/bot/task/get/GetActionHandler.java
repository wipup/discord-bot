package wp.discord.bot.task.get;

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
public class GetActionHandler implements ActionHandler {

	@Autowired
	private GetAudioTask getAudioTask;

	@Autowired
	private GetScheduleTask getScheduleTask;

	@Autowired
	private GetLogTask getLogTask;

	@Override
	public void handleAction(BotAction action) throws Exception {
		String targetEntity = SafeUtil.get(() -> action.getActionParams().get(0));
		CmdEntity entity = CmdEntity.getMatchingEntity(targetEntity);
		if (entity == null) {
			Reply rep = Reply.of().literal("Unknown entity: ").code(targetEntity);
			throw new ActionFailException(rep);
		}

		log.debug("get : {}", entity);
		if (entity == CmdEntity.AUDIO) {
			getAudioTask.getAllAudio(action);

		} else if (entity == CmdEntity.SCHEDULE) {
			getScheduleTask.handleGetSchedule(action);

		} else if (entity == CmdEntity.LOG) {
			getLogTask.getLogs(action);

		} else {
			Reply rep = Reply.of().literal("Unsupported entity: ").code(targetEntity);
			throw new ActionFailException(rep);
		}

	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.GET;
	}

}
