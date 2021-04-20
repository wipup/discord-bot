package wp.discord.bot.task.get;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.core.bot.UserManager;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.db.repository.ScheduleRepository;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.model.DiscordUserRole;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class GetScheduleTask {

	@Autowired
	private TracingHandler tracing;

	@Autowired
	private UserManager userManager;

	@Autowired
	private ScheduleRepository repository;

	public void handleGetSchedule(BotAction action) throws Exception {
		String scheduleId = action.getFirstEntitiesParam(CmdEntity.ID);
		boolean adminMode = validateAdminMode(action);

		Reply reply = null;
		if (StringUtils.isNotEmpty(scheduleId)) {
			reply = getSchedule(action, scheduleId, adminMode);
		} else {
			reply = getAllSchedules(action, adminMode);
		}

		tracing.queue(action.getEventMessageChannel().sendMessage(reply.build()));
	}

	private boolean validateAdminMode(BotAction action) throws Exception {
		boolean adminMode = false;
		boolean requiredAdmin = Boolean.TRUE.toString().equalsIgnoreCase(action.getFirstEntitiesParam(CmdEntity.ADMIN));
		if (requiredAdmin) {
			DiscordUserRole role = userManager.getRoleOf(action.getAuthorId());
			adminMode = (role == DiscordUserRole.ADMIN || role == DiscordUserRole.OWNER);
		}

		if (!adminMode && requiredAdmin) {
			Reply r = Reply.of().mentionUser(action.getAuthorId()).literal(", Only admin is allowed.");
			throw new ActionFailException(r);
		}
		return adminMode;
	}

	private Reply getAllSchedules(BotAction action, boolean adminMode) throws Exception {
		String author = action.getAuthorId();
		List<ScheduledAction> allSchedules = adminMode ? repository.findAll() : repository.findAll(author);

		if (CollectionUtils.isEmpty(allSchedules)) {
			Reply r = Reply.of().mentionUser(author).literal(", you don't have any scheduled action.");
			throw new ActionFailException(r);
		}

		return createReplyForAllSchedules(allSchedules, adminMode);
	}

	private Reply createReplyForAllSchedules(List<ScheduledAction> allSchedules, boolean adminMode) {
		Reply reply = Reply.of().bold("All Schedule IDs").newline();
		int count = 0;
		for (ScheduledAction scha : allSchedules) {
			count++;
			reply.code(String.format("%2d.) ", count)) //
					.append(scha.shortReply(adminMode)).newline();
		}
		reply.literal("To see details, type: ").code("bot get schedule id [id]");
		return reply;
	}

	private Reply getSchedule(BotAction action, String scheduleId, boolean admin) throws Exception {
		String authorId = action.getAuthorId();
		BigInteger id = SafeUtil.get(() -> new BigInteger(scheduleId));
		if (id == null) {
			Reply r = Reply.of().mentionUser(authorId).bold(" Error!").literal(" Schedule ID must be a number!");
			throw new ActionFailException(r);
		}

		ScheduledAction found = null;
		if (admin) {
			found = getScheduleAdmin(action, id, scheduleId);
		} else {
			found = getScheduleUser(authorId, id, scheduleId);
		}
		if (found == null) {
			Reply r = Reply.of().mentionUser(authorId).literal(", not found ID: ").bold(scheduleId);
			throw new ActionFailException(r);
		}

		return found.reply();
	}

	public ScheduledAction getScheduleAdmin(BotAction action, BigInteger id, String scheduleId) throws Exception {
		String userId = DiscordFormat.extractId(action.getFirstEntitiesParam(CmdEntity.USER));
		if (StringUtils.isNotBlank(userId)) {
			return repository.find(userId, id);
		} else {
			return repository.findFromAdmin(id);
		}
	}

	public ScheduledAction getScheduleUser(String author, BigInteger id, String scheduleId) throws Exception {
		return repository.find(author, id);
	}

}
