package wp.discord.bot.task.get;

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
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class GetScheduleTask {

	@Autowired
	private ScheduleRepository repository;

	public void handleGetSchedule(BotAction action) throws Exception {
		String scheduleId = action.getFirstEntitiesParam(CmdEntity.ID);

		if (StringUtils.isNotEmpty(scheduleId)) {
			getSchedule(action, scheduleId);
			return;
		}

		getAllSchedules(action);
		return;
	}

	public void getAllSchedules(BotAction action) throws Exception {
		String author = action.getAuthorId();
		List<ScheduledAction> allSchedules = repository.findAll(author);

		if (CollectionUtils.isEmpty(allSchedules)) {
			Reply r = Reply.of().mentionUser(author).literal(", you don't have any scheduled action.");
			action.getEventMessageChannel().sendMessage(r.build()).queue();
			return;
		}

		Reply reply = Reply.of().bold("All Schedule IDs").newline();
		int count = 0;
		for (ScheduledAction scha : allSchedules) {
			count++;
			reply.code(String.format("%2d.) ", count)) //
					.append(scha.shortReply()).newline();
		}
		reply.literal("To see details, type: ").code("bot get schedule id [id]");
		action.getEventMessageChannel().sendMessage(reply.build()).queue();
	}

	public void getSchedule(BotAction action, String scheduleId) throws Exception {
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
			action.getEventMessageChannel().sendMessage(found.reply().toString()).queue();
		}
	}

}
