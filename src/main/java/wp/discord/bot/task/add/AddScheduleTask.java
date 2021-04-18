package wp.discord.bot.task.add;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.entities.User;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.ScheduledActionManager;
import wp.discord.bot.core.bot.UserManager;
import wp.discord.bot.core.cmd.CommandLineProcessor;
import wp.discord.bot.core.cmd.EntityReferenceHandler;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.db.repository.ScheduleRepository;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.task.cron.CompileCronTask;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class AddScheduleTask {

	@Autowired
	private ScheduleRepository repository;

	@Autowired
	private UserManager userManager;

	@Autowired
	private EntityReferenceHandler refHandler;

	@Autowired
	private CompileCronTask cronTask;

	@Autowired
	private ScheduledActionManager scheduledActionManager;

	@Autowired
	private CommandLineProcessor cmdProcessor;

	public void addAlert(BotAction action) throws Exception {
		ScheduledAction sch = newScheduledAction(action);
		saveAlertToRepository(sch, action);

		if (isActivateSchedule(action)) {
			applyAlert(sch, action);
		}

		Reply rep = Reply.of().bold("Scheduled Command Added").literal(refHandler.generateEncodedReferenceCode(sch)).newline() //
				.append(sch.reply().buildUnquoted());
		action.getEventMessageChannel().sendMessage(rep.toString()).queue();
	}

	public boolean isActivateSchedule(BotAction action) {
		String status = action.getFirstEntitiesParam(CmdEntity.ACTIVE);
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
			throw new BotException(rep);
		}

		validateScheduledAction(sch);
		repository.save(sch);
	}

	public void validateScheduledAction(ScheduledAction sch) throws Exception {
		String authorId = sch.getAuthorId();
		User author = userManager.getUserEntity(sch.getAuthorId());
		if (author == null) {
			Reply rep = Reply.of().literal("Invalid authorId ").code(authorId);
			throw new BotException(rep);
		}

		BigInteger desiredRunCount = sch.getDesiredRunCount();
		String cron = sch.getCron();
		cronTask.parse(authorId, cron);

		if (CollectionUtils.isEmpty(sch.getCommands())) {
			Reply rep = Reply.of().literal("Commands must not be empty ").code("(cmd)");
			throw new BotException(rep);
		}
		if (sch.getCommands().size() > ScheduledAction.MAX_COMMAND_LINES) {
			Reply rep = Reply.of().literal("Too many commands!");
			throw new BotException(rep);
		}

		if (StringUtils.length(sch.getName()) > ScheduledAction.MAX_NAME) {
			Reply rep = Reply.of().literal("Name is too long!");
			throw new BotException(rep);
		}

		for (String cmd : sch.getCommands()) {
			isAllowSchedulingAction(cmdProcessor.handleCommand(null, cmd));
		}

		if (desiredRunCount != null) {
//			if (desiredRunCount <= 0) {
			if (desiredRunCount.compareTo(BigInteger.ZERO) <= 0) {
				Reply rep = Reply.of().literal("Desired run count must more than zero! " + desiredRunCount);
				throw new BotException(rep);
			}
			
			BigInteger actualRun = sch.getActualRunCount();
			if (actualRun.compareTo(desiredRunCount) >= 0) {
				Reply rep = Reply.of().literal("Actual run count has already exceeded desired run count! ");
				throw new BotException(rep);
			}
		}
	}

	public ScheduledAction newScheduledAction(BotAction action) throws Exception {
		BigInteger runCount = parseDesiredRunCount(action);
		String cron = StringUtils.join(action.getEntities(CmdEntity.CRON), " ");
		String name = action.getFirstEntitiesParam(CmdEntity.NAME);
		List<String> cmds = action.getEntities(CmdEntity.CMD);
		String authorId = action.getAuthorId();

		ScheduledAction sch = new ScheduledAction();
		sch.setAuthorId(authorId);
		sch.setCommands(cmds);
		sch.setCron(cron);
		sch.setName(StringUtils.defaultString(name));
		sch.setId(repository.nextSeqId());
		sch.setActualRunCount(BigInteger.ZERO);
		sch.setScheduledTask(null);
		sch.setActive(false);
		if (runCount != null) {
			sch.setDesiredRunCount(runCount);
		}
		return sch;
	}

	public BigInteger parseDesiredRunCount(BotAction action) throws Exception {
		String desiredRunCount = action.getFirstEntitiesParam(CmdEntity.COUNT);
		BigInteger runCount = null;
		if (StringUtils.isNotEmpty(desiredRunCount)) {
			runCount = SafeUtil.get(() -> new BigInteger(desiredRunCount));
			if (runCount == null) {
				Reply reply = Reply.of().literal("Error! ").literal("Invalid run count: ").code(desiredRunCount);
				throw new BotException(reply);
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
		throw new BotException(r);
	}
}
