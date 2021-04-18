package wp.discord.bot.task.update;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.db.repository.ScheduleRepository;
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

	public void handleUpdateSchedule(BotAction action) throws Exception {
		String author = action.getAuthorId();
		String scheduleId = action.getFirstEntitiesParam(CmdEntity.ID);

		if (StringUtils.isEmpty(scheduleId)) {
			Reply r = Reply.of().mentionUser(author).literal(" Schedule ID is required!");
			action.getEventMessageChannel().sendMessage(r.build()).queue();
			return;
		}

		updateSchedule(action, scheduleId);
	}

	public void updateSchedule(BotAction action, String scheduleId) throws Exception {
		String author = action.getAuthorId();
		BigInteger id = SafeUtil.get(() -> new BigInteger(scheduleId));
		if (id == null) {
			Reply r = Reply.of().mentionUser(author).bold(" Error!").literal(" Schedule ID must be a number!");
			action.getEventMessageChannel().sendMessage(r.build()).queue();
			return;
		}

		ScheduledAction found = repository.find(author, id);
		if (found == null) {
			Reply r = Reply.of().mentionUser(author).literal(", not found ID: ").bold(scheduleId);
			action.getEventMessageChannel().sendMessage(r.build()).queue();
			return;
		}

		synchronized (found) {
			updateSchedule(action, found);
			repository.save(found);

			Reply rep = Reply.of().bold("Update Schedule Task Completed").newline().append(found.reply());
			action.getEventMessageChannel().sendMessage(rep.build()).queue();
		}
	}

	public void updateSchedule(BotAction action, ScheduledAction schedule) throws Exception {
		boolean requireRescheduled = false;

		List<String> cronExpr = action.getEntities(CmdEntity.CRON);
		if (CollectionUtils.isNotEmpty(cronExpr)) {
			String cron = StringUtils.join(cronExpr, " ");
			schedule.setCron(cron);
			requireRescheduled = true;
		}

		String name = action.getFirstEntitiesParam(CmdEntity.NAME);
		if (StringUtils.isNotBlank(name)) {
			schedule.setName(name);
		}
		
		addTask.validateScheduledAction(schedule);

		String status = action.getFirstEntitiesParam(CmdEntity.ACTIVE);
		if (schedule.isActive()) { // already active
			if (requireRescheduled || Boolean.TRUE.toString().equalsIgnoreCase(status)) {
				schedule.getScheduledTask().cancel(true);
				schedule.setScheduledTask(null);
				addTask.applyAlert(schedule, action);
				return;
			}

			if (Boolean.FALSE.toString().equalsIgnoreCase(status)) {
				schedule.getScheduledTask().cancel(true);
				schedule.setScheduledTask(null);
				return;
			}
		} else { // currently not active
			if (Boolean.TRUE.toString().equalsIgnoreCase(status)) {
				addTask.applyAlert(schedule, action);
			}
		}
	}

	
}





















