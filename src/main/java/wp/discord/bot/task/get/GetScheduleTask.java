package wp.discord.bot.task.get;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.core.RoleEnforcer;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.db.repository.ScheduleRepository;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class GetScheduleTask {

	@Autowired
	private RoleEnforcer roleEnforcer;

	@Autowired
	private TracingHandler tracing;

	@Autowired
	private ScheduleRepository repository;

	public void handleGetSchedule(BotAction action) throws Exception {
		String scheduleId = action.getFirstTokenParam(CmdToken.ID);

		Reply reply = null;
		if (StringUtils.isNotEmpty(scheduleId)) {
			reply = getScheduleReply(action, scheduleId);
		} else {
			reply = getAllSchedulesReply(action);
		}

		tracing.queue(action.getEventMessageChannel().sendMessage(reply.build()));
	}

	private boolean isAdminMode(BotAction action) throws Exception {
		boolean requiredAdmin = Boolean.TRUE.toString().equalsIgnoreCase(action.getFirstTokenParam(CmdToken.ADMIN));
		if (requiredAdmin) {
			roleEnforcer.allowOnlyAdminOrHigher(action);
			return true;
		}
		return false;
	}

	private Reply getScheduleReply(BotAction action, String scheduleId) throws Exception {
		ScheduledAction found = getSchedule(action, scheduleId);
		if (found == null) {
			Reply r = Reply.of().mentionUser(action.getAuthorId()).literal(", not found ID: ").bold(scheduleId);
			throw new ActionFailException(r);
		}
		return found.reply();
	}

	public ScheduledAction getSchedule(BotAction action, String scheduleId) throws Exception {
		boolean admin = isAdminMode(action);

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
		return found;
	}

	public Reply getAllSchedulesReply(BotAction action) throws Exception {
		List<ScheduledAction> allSchedules = getAllSchedules(action);
		return createReplyForAllSchedules(action, allSchedules);
	}

	public List<ScheduledAction> getAllSchedules(BotAction action) throws Exception {
		boolean adminMode = isAdminMode(action);

		String author = action.getAuthorId();
		String userId = action.getFirstTokenParam(CmdToken.USER);
		if (StringUtils.isNotEmpty(userId)) {
			roleEnforcer.allowOnlyAdminOrHigher(action);
			author = DiscordFormat.extractId(userId);
		}

		List<ScheduledAction> allSchedules = adminMode ? repository.findAll() : repository.findAll(author);

		if (CollectionUtils.isEmpty(allSchedules)) {
			Reply r = Reply.of().mentionUser(author).literal(", you don't have any scheduled action.");
			throw new ActionFailException(r);
		}

		return allSchedules;
	}

	public Reply createReplyForAllSchedules(BotAction action, List<ScheduledAction> allSchedules) throws Exception {
		boolean adminMode = isAdminMode(action);

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

	private ScheduledAction getScheduleAdmin(BotAction action, BigInteger id, String scheduleId) throws Exception {
		String userId = DiscordFormat.extractId(action.getFirstTokenParam(CmdToken.USER));
		if (StringUtils.isNotBlank(userId)) {
			return repository.find(userId, id);
		} else {
			return repository.findFromAdmin(id);
		}
	}

	private ScheduledAction getScheduleUser(String author, BigInteger id, String scheduleId) throws Exception {
		return repository.find(author, id);
	}

}
