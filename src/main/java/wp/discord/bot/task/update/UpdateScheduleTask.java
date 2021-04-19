package wp.discord.bot.task.update;

import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.db.repository.ScheduleRepository;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.task.add.AddScheduleTask;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class UpdateScheduleTask {

	@Autowired
	private AddScheduleTask addTask;

	@Autowired
	private ScheduleRepository repository;

	@Autowired
	private TracingHandler tracing;

	public void handleUpdateSchedule(BotAction action) throws Exception {
		String author = action.getAuthorId();
		String scheduleId = action.getFirstEntitiesParam(CmdEntity.ID);

		if (StringUtils.isEmpty(scheduleId)) {
			Reply r = Reply.of().mentionUser(author).literal(" Schedule ID is required!");
			throw new ActionFailException(r);
		}

		updateSchedule(action, scheduleId);
	}

	public void updateSchedule(BotAction action, String scheduleId) throws Exception {
		String author = action.getAuthorId();
		BigInteger id = SafeUtil.get(() -> new BigInteger(scheduleId));
		if (id == null) {
			Reply r = Reply.of().mentionUser(author).bold(" Error!").literal(" Schedule ID must be a number!");
			throw new ActionFailException(r);
		}

		ScheduledAction found = repository.find(author, id);
		if (found == null) {
			Reply r = Reply.of().mentionUser(author).literal(", not found ID: ").bold(scheduleId);
			throw new ActionFailException(r);
		}

		synchronized (found) {
			updateSchedule(action, found);
			repository.save(found);

			Reply rep = Reply.of().bold("Update Schedule Task Completed").newline().append(found.reply());
			tracing.queue(action.getEventMessageChannel().sendMessage(rep.build()));
		}
	}

	private boolean updateScheduleType(BotAction action, ScheduledAction schedule) throws Exception {
		String cron = StringUtils.join(action.getEntities(CmdEntity.CRON), " ").trim();
		String time = StringUtils.defaultString(action.getFirstEntitiesParam(CmdEntity.TIME)).trim();
		String every = StringUtils.defaultString(action.getFirstEntitiesParam(CmdEntity.EVERY)).trim();

		if (StringUtils.firstNonBlank(cron, time, every) != null) {
			schedule.setPreference(addTask.getScheduleType(action));
			return true;
		}
		return false;
	}
	
	public void updateSchedule(BotAction action, ScheduledAction schedule) throws Exception {
		boolean requireRescheduled = updateScheduleType(action, schedule);

		String name = action.getFirstEntitiesParam(CmdEntity.NAME);
		if (StringUtils.isNotBlank(name)) {
			schedule.setName(name);
		}

		BigInteger desiredRunCount = addTask.parseDesiredRunCount(action);
		if (desiredRunCount != null) {
			schedule.setDesiredRunCount(desiredRunCount);
		}

		addTask.validateScheduledAction(schedule);

		String status = action.getFirstEntitiesParam(CmdEntity.ACTIVE);
		if (schedule.isActive()) { // already active
			if (requireRescheduled || Boolean.TRUE.toString().equalsIgnoreCase(status)) {
				schedule.getScheduledTask().cancel(true);
				schedule.setScheduledTask(null);
				schedule.setActive(false);
				addTask.applyAlert(schedule, action);
				return;
			}

			if (Boolean.FALSE.toString().equalsIgnoreCase(status)) {
				schedule.getScheduledTask().cancel(true);
				schedule.setScheduledTask(null);
				schedule.setActive(false);
				return;
			}
		} else { // currently not active
			if (Boolean.TRUE.toString().equalsIgnoreCase(status)) {
				addTask.applyAlert(schedule, action);
			}
		}
	}

}
