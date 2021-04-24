package wp.discord.bot.task.delete;

import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.db.repository.ScheduleRepository;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.task.get.GetScheduleTask;
import wp.discord.bot.util.Reply;

@Component
@Slf4j
public class DeleteScheduleTask {

	@Autowired
	private GetScheduleTask getTask;

	@Autowired
	private ScheduleRepository repository;

	@Autowired
	private TracingHandler tracing;

	public void deleteSchedule(BotAction action) throws Exception {
		String scheduleId = action.getFirstTokenParam(CmdToken.ID);

		if (StringUtils.isEmpty(scheduleId)) {
			Reply r = Reply.of().mentionUser(action.getAuthorId()).bold(" Error!").literal(" Schedule ID is required!").newline() //
					.literal("To delete, type: ").code("bot delete schedule id [id]");
			throw new ActionFailException(r);
		}

		deleteSchedule(action, scheduleId);
	}

	public void deleteSchedule(BotAction action, String scheduleId) throws Exception {
		ScheduledAction found = getTask.getSchedule(action, scheduleId);
		if (found == null) {
			Reply r = Reply.of().mentionUser(action.getAuthorId()).literal(", not found ID: ").bold(scheduleId);
			throw new ActionFailException(r);
		}

		synchronized (found) {
			deleteSchedule(action, found);
		}
	}

	public void deleteSchedule(BotAction action, ScheduledAction found) throws Exception {
		repository.delete(found);

		Future<?> task = found.getScheduledTask();
		if (task != null) {
			log.debug("deleting active: {}", found);
			task.cancel(true);
			found.setScheduledTask(null);
			found.setActive(false);
		} else {
			log.debug("deleting: {}", found);
		}

		Reply reply = Reply.of().bold("Scheduled Task Deleted").newline() //
				.append(found.reply());
		tracing.queue(action.getEventMessageChannel().sendMessage(reply.toString()));
	}

}
