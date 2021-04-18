package wp.discord.bot.task.get;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.bot.UserManager;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.db.repository.ScheduleRepository;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.model.DiscordUserRole;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class GetScheduleTask {

	@Autowired
	private UserManager userManager;

	@Autowired
	private ScheduleRepository repository;

	public void handleGetSchedule(BotAction action) throws Exception {
		String scheduleId = action.getFirstEntitiesParam(CmdEntity.ID);

		String isAdmin = action.getFirstEntitiesParam(CmdEntity.ADMIN);
		if ("true".equalsIgnoreCase(isAdmin)) {
			DiscordUserRole role = userManager.getRoleOf(action.getAuthorId());
			if (role == DiscordUserRole.ADMIN || role == DiscordUserRole.OWNER) {
				getSchedule(action, scheduleId, true);
				return;
			} else {
				Reply r = Reply.of().mentionUser(action.getAuthorId()).literal(", Only admin is allowed.");
				action.getEventMessageChannel().sendMessage(r.build()).queue();
				return;
			}
		}

		if (StringUtils.isNotEmpty(scheduleId)) {
			getSchedule(action, scheduleId, false);
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

	public void getSchedule(BotAction action, String scheduleId, boolean admin) throws Exception {
		String authorId = action.getAuthorId();
		BigInteger id = SafeUtil.get(() -> new BigInteger(scheduleId));
		if (id == null) {
			Reply r = Reply.of().mentionUser(authorId).bold(" Error!").literal(" Schedule ID must be a number!");
			action.getEventMessageChannel().sendMessage(r.build()).queue();
			return;
		}

		ScheduledAction found = null;
		if (admin) {
			found = getScheduleAdmin(id, scheduleId);
		} else {
			found = getScheduleUser(authorId, id, scheduleId);
		}
		if (found == null) {
			Reply r = Reply.of().mentionUser(authorId).literal(", not found ID: ").bold(scheduleId);
			action.getEventMessageChannel().sendMessage(r.build()).queue();
			return;
		}

		synchronized (found) {
			action.getEventMessageChannel().sendMessage(found.reply().toString()).queue();
		}
	}

	public ScheduledAction getScheduleAdmin(BigInteger id, String scheduleId) throws Exception {
		return repository.findFromAdmin(id);

	}

	public ScheduledAction getScheduleUser(String author, BigInteger id, String scheduleId) throws Exception {
		return repository.find(author, id);
	}

}
