package wp.discord.bot.task.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class UpdateActionHandler implements ActionHandler {

	@Autowired
	private UpdateScheduleTask updateAlertTask;

	@Autowired
	private UpdateAudioTrackTask updateAudioTask;

	@Override
	public void handleAction(BotAction action) throws Exception {
		String targetEntity = SafeUtil.get(() -> action.getActionParams().get(0));
		CmdToken entity = CmdToken.getMatchingCmdToken(targetEntity);
		if (entity == null) {
			Reply rep = Reply.of().literal("Unknown entity: ").code(targetEntity);
			throw new ActionFailException(rep);
		}

		log.debug("updating : {}", entity);
		if (entity == CmdToken.SCHEDULE) {
			updateAlertTask.handleUpdateSchedule(action);
			return;
		}

		if (entity == CmdToken.AUDIO) {
			updateAudioTask.reloadAllAudioTracks(action);
			return;
		}

		if (entity == CmdToken.AUTO_REPLY) {
			// TODO
		}

		Reply rep = Reply.of().literal("Unsupported updating entity: ").code(targetEntity);
		throw new ActionFailException(rep);
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.UPDATE;
	}

}
