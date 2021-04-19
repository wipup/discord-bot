package wp.discord.bot.task.delete;

import java.math.BigInteger;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.db.repository.ScheduleRepository;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class DeleteScheduleTask {

	@Autowired
	private ScheduleRepository repository;

	@Autowired
	private TracingHandler tracing;
	
	public void deleteSchedule(BotAction action) throws Exception {
		String author = action.getAuthorId();
		String scheduleId = action.getFirstEntitiesParam(CmdEntity.ID);

		if (StringUtils.isEmpty(scheduleId)) {
			Reply r = Reply.of().mentionUser(author).bold(" Error!").literal(" Schedule ID is required!").newline() //
					.literal("To delete, type: ").code("bot delete schedule id [id]");
			throw new ActionFailException(r);
		}

		deleteSchedule(action, scheduleId);
	}

	public void deleteSchedule(BotAction action, String scheduleId) throws Exception {
		String author = action.getAuthorId();
		BigInteger id = SafeUtil.get(() -> new BigInteger(scheduleId));
		if (id == null) {
			Reply r = Reply.of().mentionUser(author).bold(" Error!").literal(" Schedule ID must be a number!");
			action.getEventMessageChannel().sendMessage(r.build()).queue();
			throw new ActionFailException(r);
		}

		ScheduledAction found = repository.find(author, id);
		if (found == null) {
			Reply r = Reply.of().mentionUser(author).literal(", not found ID: ").bold(scheduleId);
			action.getEventMessageChannel().sendMessage(r.build()).queue();
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
