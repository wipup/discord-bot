package wp.discord.bot.task.add;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.entities.User;
import wp.discord.bot.constant.BotReferenceConstant;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.core.ScheduledActionManager;
import wp.discord.bot.core.bot.UserManager;
import wp.discord.bot.core.cmd.CommandLineProcessor;
import wp.discord.bot.core.cmd.EntityReferenceHandler;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.db.entity.ScheduledOption;
import wp.discord.bot.db.entity.ScheduledType;
import wp.discord.bot.db.repository.ScheduleRepository;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.task.cron.CompileCronActionHandler;
import wp.discord.bot.task.cron.CronEntity;
import wp.discord.bot.util.DateTimeUtil;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;
import wp.discord.bot.util.ToStringUtils;

@Component
public class AddScheduleTask {

	@Autowired
	private ScheduleRepository repository;

	@Autowired
	private UserManager userManager;

	@Autowired
	private EntityReferenceHandler refHandler;

	@Autowired
	private CompileCronActionHandler cronTask;

	@Autowired
	private ScheduledActionManager scheduledActionManager;

	@Autowired
	private CommandLineProcessor cmdProcessor;

	public void addAlert(BotAction action) throws Exception {
		ScheduledAction sch = newScheduledAction(action);
		saveAlertToRepository(sch, action);

		if (isActivateSchedule(action)) {
			applyAlert(sch, action);
			saveAlertToRepository(sch, action);
		}

		Reply rep = Reply.of().bold("Scheduled Command Added").literal(refHandler.generateEncodedReferenceCode(sch)).newline() //
				.append(sch.reply().buildUnquoted());
		action.getEventMessageChannel().sendMessage(rep.toString()).queue();
	}

	public boolean isActivateSchedule(BotAction action) {
		String status = action.getFirstEntitiesParam(CmdToken.ACTIVE);
		if (StringUtils.isEmpty(status)) {
			status = Boolean.TRUE.toString();
		}
		return Boolean.TRUE.toString().equalsIgnoreCase(status);
	}

	public void applyAlert(ScheduledAction sch, BotAction action) throws Exception {
		try {
			ScheduledFuture<?> future = scheduledActionManager.scheduleCronTask(sch);
			sch.setScheduledTask(future);
			sch.setActive(true);

		} catch (Exception e) {
			sch.setScheduledTask(null);
			sch.setActive(false);
			saveAlertToRepository(sch, action);
			throw e;
		}
	}

	public void saveAlertToRepository(ScheduledAction sch, BotAction action) throws Exception {
		if (repository.isFull(action.getEventAuthor())) {
			Reply rep = Reply.of().literal("You have too many scheduled commands!").newline() //
					.mention(action.getEventAuthor()).literal(" Please remove some scheduled command first.");
			throw new ActionFailException(rep);
		}

		validateScheduledAction(sch);
		repository.save(sch);
	}

	private void validateScheduledActionCommands(ScheduledAction sch) throws Exception {
		if (CollectionUtils.isEmpty(sch.getCommands())) {
			Reply rep = Reply.of().literal("Commands must not be empty ").code("(cmd)");
			throw new ActionFailException(rep);
		}
		if (sch.getCommands().size() > ScheduledAction.MAX_COMMAND_LINES) {
			Reply rep = Reply.of().literal("Too many commands!");
			throw new ActionFailException(rep);
		}
		for (String cmd : sch.getCommands()) {
			isAllowSchedulingAction(cmdProcessor.handleCommand(null, cmd));
		}
	}

	private void validateScheduledDesiredRunCount(ScheduledAction sch) throws Exception {
		BigInteger desiredRunCount = sch.getDesiredRunCount();
		if (desiredRunCount != null) {
			if (desiredRunCount.compareTo(BigInteger.ZERO) <= 0) {
				Reply rep = Reply.of().literal("Desired run count must more than zero! " + desiredRunCount);
				throw new ActionFailException(rep);
			}

			BigInteger actualRun = sch.getActualRunCount();
			if (actualRun.compareTo(desiredRunCount) >= 0) {
				Reply rep = Reply.of().literal("Actual run count has already exceeded desired run count! ");
				throw new ActionFailException(rep);
			}
		}
	}

	public void validateScheduledAction(ScheduledAction sch) throws Exception {
		String authorId = sch.getAuthorId();
		User author = userManager.getUserEntity(sch.getAuthorId());
		if (author == null) {
			Reply rep = Reply.of().literal("Invalid authorId ").code(authorId);
			throw new ActionFailException(rep);
		}

		if (StringUtils.length(sch.getName()) > ScheduledAction.MAX_NAME) {
			Reply rep = Reply.of().literal("Name is too long!");
			throw new ActionFailException(rep);
		}

		validateScheduledOption(sch);
		validateScheduledActionCommands(sch);
		validateScheduledDesiredRunCount(sch);
	}

	public void validateScheduledOption(ScheduledOption opt) throws Exception {
		if (opt == null || opt.getType() == null) {
			Reply reply = Reply.of().literal("Schedule Type: ").code("Cron") //
					.literal(" or ").code("duration").literal(" time is required").newline();
			throw new ActionFailException(reply);
		}
		if (opt.getType() == ScheduledType.CRON) {
			validateCron(opt.getValue());
		} else if (opt.getType() == ScheduledType.FIXED_RATE) {
			validateDuration(opt.getValue());
		} else {
			if (opt.getStartTime() == null) {
				Reply reply = Reply.of().literal("Start-time is required!");
				throw new ActionFailException(reply);
			}
		}
	}

	public void validateScheduledOption(ScheduledAction sch) throws Exception {
		ScheduledOption opt = sch.getPreference();
		validateScheduledOption(opt);
	}

	public ScheduledAction newScheduledAction(BotAction action) throws Exception {
		BigInteger runCount = parseDesiredRunCount(action);
		String name = action.getFirstEntitiesParam(CmdToken.NAME);
		List<String> cmds = action.getEntities(CmdToken.CMD);
		String authorId = action.getAuthorId();

		ScheduledAction sch = new ScheduledAction();
		sch.setAuthorId(authorId);
		sch.setCommands(cmds);
		sch.setName(StringUtils.defaultString(name));
		sch.setId(repository.nextSeqId());
		sch.setActualRunCount(BigInteger.ZERO);
		sch.setScheduledTask(null);
		sch.setActive(false);
		if (runCount != null) {
			sch.setDesiredRunCount(runCount);
		}
		sch.setPreference(getScheduleType(action));
		return sch;
	}

	public ScheduledOption getScheduleType(BotAction action) throws Exception {
		String cron = StringUtils.join(action.getEntities(CmdToken.CRON), " ");
		String time = StringUtils.defaultString(action.getFirstEntitiesParam(CmdToken.TIME)).trim();
		String repeat = StringUtils.defaultString(action.getFirstEntitiesParam(CmdToken.REPEAT)).trim();

		if (StringUtils.isNotBlank(time) && StringUtils.isNotBlank(cron)) {
			Reply reply = Reply.of().literal("Cron and Time must be mutually exclusive!");
			throw new ActionFailException(reply);
		}

		if (StringUtils.isNotBlank(time)) {

			Date startTime = validateStartTime(time);
			Duration d = null;
			if (StringUtils.isNotBlank(repeat)) {
				d = validateDuration(repeat);
			}

			if (d == null) {
				return ScheduledOption.AtTime(startTime);
			} else {
				if (BotReferenceConstant.TIME_NOW.equalsIgnoreCase(time)) {
					startTime = DateTimeUtil.addDurationToDate(d, startTime);
				}
				return ScheduledOption.fixedRate(d.toString(), startTime);
			}
		}

		if (StringUtils.isNotBlank(cron)) {
			cron = validateCron(cron);
			return ScheduledOption.cron(cron);
		}

		Reply reply = Reply.of().literal("At least one of ").code("Cron").literal(" or ").code(" Time").literal(" must exist!");
		throw new ActionFailException(reply);
	}

	public String validateCron(String cron) throws Exception {
		CronEntity ce = cronTask.parse(cron);
		if (ce == null) {
			Reply reply = Reply.of().literal("Invalid cron: ").code(cron).newline(); //
			throw new ActionFailException(reply);
		}
		return ce.getExpression();
	}

	private Date validateStartTime(String dt) throws Exception {
		try {
			if (BotReferenceConstant.TIME_NOW.equalsIgnoreCase(dt)) {
				return new Date();
			}
			String[] frags = dt.split("\\s+");
			String next = SafeUtil.get(() -> frags[0]);
			if (BotReferenceConstant.TIME_NEXT.equalsIgnoreCase(next)) {
				String duration = SafeUtil.get(() -> frags[1]);
				Duration d = validateDuration(duration);
				return DateTimeUtil.addDurationToDate(d, new Date());
			}
			return ToStringUtils.parseDate(dt, ScheduledOption.START_DATE_FORMAT);
		} catch (Exception e) {
			Reply r = Reply.of().literal("Invalid Date format! ").code(dt).newline() //
					.literal("Expected format: ").code("yyyy-MM-ddTHH:mm:ss").newline() //
					.literal("\tOr: ").code("now").literal(" , ").code("\"next PTnDnHnMn.nS\"") //
					.literal(" , ").code("\"next n<U>\"");
			throw new ActionFailException(r);
		}
	}

	public Duration validateDuration(String duration) throws Exception {
		if (BotReferenceConstant.NONE.equalsIgnoreCase(duration)) {
			return null;
		}
		Duration d = ToStringUtils.convertToDuration(duration);
		if (d == null) {
			Reply reply = Reply.of().literal("Invalid duration-time: ").code(duration).newline() //
					.literal("Duration time must match ").bold("ISO-8601").literal(" DURATION format with pattern ").code("PnDTnHnMn.nS");
			throw new ActionFailException(reply);
		}
		return d;
	}

	public BigInteger parseDesiredRunCount(BotAction action) throws Exception {
		String desiredRunCount = action.getFirstEntitiesParam(CmdToken.COUNT);
		BigInteger runCount = null;
		if (StringUtils.isNotEmpty(desiredRunCount)) {
			if (desiredRunCount.equalsIgnoreCase(BotReferenceConstant.NONE) || desiredRunCount.equalsIgnoreCase(BotReferenceConstant.INFINITY)) {
				return null;
			}

			runCount = SafeUtil.get(() -> new BigInteger(desiredRunCount));
			if (runCount == null) {
				Reply reply = Reply.of().literal("Error! ").literal("Invalid run count: ").code(desiredRunCount);
				throw new ActionFailException(reply);
			}
		}
		return runCount;
	}

	private void isAllowSchedulingAction(BotAction action) throws Exception {
		if (action == null) {
			return;
		}
		CmdAction act = action.getAction();
		for (CmdAction allow : ScheduledAction.SCHEDULABLE_ACTIONS) {
			if (act == allow) {
				return;
			}
		}

		Reply r = Reply.of().literal("Error! Command not allow for scheduling: ").code(act.getCmd());
		throw new ActionFailException(r);
	}
}
