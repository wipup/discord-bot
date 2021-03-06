package wp.discord.bot.task.update;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.constant.BotReferenceConstant;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.db.entity.ScheduledOption;
import wp.discord.bot.db.repository.ScheduleRepository;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.task.add.AddScheduleTask;
import wp.discord.bot.task.get.GetScheduleTask;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;
import wp.discord.bot.util.ToStringUtils;

@Slf4j
@Component
public class UpdateScheduleTask {

	@Autowired
	private AddScheduleTask addTask;

	@Autowired
	private GetScheduleTask getTask;

	@Autowired
	private ScheduleRepository repository;

	@Autowired
	private TracingHandler tracing;

	public void handleUpdateSchedule(BotAction action) throws Exception {
		String author = action.getAuthorId();
		String scheduleId = action.getFirstTokenParam(CmdToken.ID);

		if (StringUtils.isEmpty(scheduleId)) {
			Reply r = Reply.of().mentionUser(author).literal(" Schedule ID is required!");
			throw new ActionFailException(r);
		}

		updateSchedule(action, scheduleId);
	}

	public void updateSchedule(BotAction action, String scheduleId) throws Exception {
		ScheduledAction found = getTask.getSchedule(action, scheduleId);
		if (found == null) {
			Reply r = Reply.of().mentionUser(action.getAuthorId()).literal(", not found ID: ").bold(scheduleId);
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
		Collection<String> cron = action.getAllTokenParams(CmdToken.CRON);
		String cronStr = StringUtils.join(cron, " ").trim();
		String time = StringUtils.defaultString(action.getFirstTokenParam(CmdToken.TIME)).trim();
		String every = StringUtils.defaultString(action.getFirstTokenParam(CmdToken.REPEAT)).trim();

		if (BotReferenceConstant.SAME_VALUE.equalsIgnoreCase(time)) {
			String newTime = SafeUtil.get(() -> ToStringUtils.formatDate(schedule.getPreference().getStartTime(), ScheduledOption.START_DATE_FORMAT));
			log.debug("set time from: {} to old value: {}", time, newTime);
			action.getAllTokenParams(CmdToken.TIME).clear();
			action.getAllTokenParams(CmdToken.TIME).add(newTime);
		}
		if (BotReferenceConstant.SAME_VALUE.equalsIgnoreCase(every)) {
			String newRepeat = schedule.getPreference().getValue();
			log.debug("set every-duration from: {} to old value: {}", every, newRepeat);
			action.getAllTokenParams(CmdToken.REPEAT).clear();
			action.getAllTokenParams(CmdToken.REPEAT).add(newRepeat);
		}

		String timeOrEvery = StringUtils.firstNonBlank(time, every, cronStr);
		if (timeOrEvery != null) {
			ScheduledOption opt = addTask.getScheduleType(action);
			if (opt == null) {
				Reply reply = Reply.of().literal("Error! Invalid time/duration/cron configuration. ").newline() //
						.literal("Value: ").code(timeOrEvery);
				throw new ActionFailException(reply);
			}
			schedule.setPreference(opt);
			return true;
		}
		return false;
	}

	public void updateSchedule(BotAction action, ScheduledAction schedule) throws Exception {
		boolean requireRescheduled = updateScheduleType(action, schedule);

		String name = action.getFirstTokenParam(CmdToken.NAME);
		if (StringUtils.isNotBlank(name)) {
			schedule.setName(name);
		}

		String desiredRunCountStr = action.getFirstTokenParam(CmdToken.COUNT);
		if (StringUtils.isNotBlank(desiredRunCountStr)) {
			BigInteger desiredRunCount = addTask.parseDesiredRunCount(action);
			schedule.setDesiredRunCount(desiredRunCount);
		}

		List<String> cmds = action.getAllTokenParams(CmdToken.CMD);
		if (CollectionUtils.isNotEmpty(cmds)) {
			schedule.setCommands(cmds);
			requireRescheduled = true;
		}

		addTask.validateScheduledAction(schedule);

		String status = action.getFirstTokenParam(CmdToken.ACTIVE);
		if (schedule.isActive()) { // already active
			if (requireRescheduled || Boolean.TRUE.toString().equalsIgnoreCase(status)) {
				schedule.getScheduledTask().cancel(true);
				schedule.setScheduledTask(null);
				schedule.setActive(false);
//				schedule.setActualRunCount(BigInteger.ZERO);
				addTask.applyAlert(schedule, action);
				return;
			}

			if (Boolean.FALSE.toString().equalsIgnoreCase(status)) {
				log.debug("Cancel scheduled action: {}", action);
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
