package wp.discord.bot.core;

import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import wp.discord.bot.config.AsyncConfig;
import wp.discord.bot.core.action.ActionHandleManager;
import wp.discord.bot.core.cmd.CommandLineProcessor;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.db.entity.ScheduledOption;
import wp.discord.bot.db.entity.ScheduledType;
import wp.discord.bot.db.repository.ScheduleRepository;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class ScheduledActionManager implements DisposableBean {

	@Autowired
	@Qualifier(AsyncConfig.BEAN_CRON_TASK_DECORATOR)
	private TaskDecorator taskDecorator;

	@Autowired
	private TracingHandler tracer;

	@Autowired
	@Qualifier(AsyncConfig.BEAN_CRON_TASK_EXECUTOR)
	private ScheduledExecutorService executorService;

	@Autowired
	@Qualifier(AsyncConfig.BEAN_CRON_TASK_SCHEDULER)
	private TaskScheduler cronScheduler;

	@Autowired
	private JDA jda;

	@Autowired
	@Qualifier(AsyncConfig.BEAN_UNLIMIT_EXECUTOR)
	private ExecutorService unlimitExecutor;

	@Autowired
	private CommandLineProcessor cmdProcessor;

	@Autowired
	private ActionHandleManager actionManager;

	@Autowired
	private EventErrorHandler errorHandler;

	@Autowired
	private ScheduleRepository scheduleRepository;

	public ScheduledFuture<?> scheduleCronTask(ScheduledAction scheduleAction) throws Exception {

		ScheduledFuture<?> future = null;
		ScheduledOption opt = scheduleAction.getPreference();

		if (opt.getType() == ScheduledType.CRON) {
			CronTrigger cron = new CronTrigger(opt.getValue());
			Runnable runnable = taskDecorator.decorate(newRunnableScheduledAction(scheduleAction));
			future = cronScheduler.schedule(runnable, cron);

		} else if (opt.getType() == ScheduledType.FIXED_RATE) {
			Duration duration = Duration.parse(opt.getValue());
			Runnable runnable = taskDecorator.decorate(newRunnableScheduledAction(scheduleAction));
			future = cronScheduler.scheduleAtFixedRate(runnable, opt.getStartTime().toInstant(), duration);

		} else if (opt.getType() == ScheduledType.TIME) {
			Runnable runnable = taskDecorator.decorate(newRunnableScheduledAction(scheduleAction));
			future = cronScheduler.schedule(runnable, opt.getStartTime().toInstant());
		}

		scheduleAction.setScheduledTask(future);
		scheduleAction.setActive(true);
		return future;
	}

	private Runnable newRunnableScheduledAction(ScheduledAction scheduleAction) {
		log.debug("scheduling: {}", scheduleAction);
		return () -> {
			try {
				if (isRunningCountExceed(scheduleAction)) {
					return;
				}

				scheduleAction.setActualRunCount(scheduleAction.getActualRunCount().add(BigInteger.ONE));
				for (String cmd : scheduleAction.getCommands()) {
					BotAction action = null;
					try {
						action = cmdProcessor.handleCommand(null, cmd);
						if (action != null) {
							action.setAuthorId(scheduleAction.getAuthorId());
							action.setFromScheduler(true);
							actionManager.executeAction(action);
						}

					} catch (Exception e) {
						log.debug("Error executing schedule: {}", scheduleAction.getId(), e);
						handleScheduleTaskFail(scheduleAction, action, e);
					} catch (Throwable e) {
						handleScheduleTaskFail(scheduleAction, action, e);
						throw e;
					}
				}

			} finally {
				if (scheduleAction.getPreference().getType() == ScheduledType.TIME) {
					scheduleAction.setActive(false);
				}
				SafeUtil.suppress(() -> scheduleRepository.save(scheduleAction));
			}
		};
	}

	private boolean isRunningCountExceed(ScheduledAction scheduleAction) {
		BigInteger actual = ObjectUtils.defaultIfNull(scheduleAction.getActualRunCount(), BigInteger.ZERO);
		BigInteger preferred = scheduleAction.getDesiredRunCount();
		if (preferred != null) {
			if (actual.compareTo(preferred) >= 0) {
				cancelTask(scheduleAction);
				return true;
			}
		}

		return false;
	}

	private void cancelTask(ScheduledAction scheduleAction) {
		log.debug("cancelling count-exceed task: {}", scheduleAction.getName());
		ScheduledFuture<?> future = scheduleAction.getScheduledTask();

		SafeUtil.suppress(() -> Thread.sleep(50));
		unlimitExecutor.execute(taskDecorator.decorate(() -> {
			log.debug("do cancel count-exceed task: {}", scheduleAction.getName());
			future.cancel(true);
			scheduleAction.setScheduledTask(null);
			scheduleAction.setActive(false);
			SafeUtil.suppress(() -> scheduleRepository.save(scheduleAction));
		}));
		SafeUtil.suppress(() -> Thread.sleep(50));
	}

	private void handleScheduleTaskFail(ScheduledAction scheduleAction, BotAction action, Throwable e) {
		if (e instanceof BotException) {
			BotException be = (BotException) e;
			unlimitExecutor.submit(taskDecorator.decorate(() -> {
				notifyAuthor(scheduleAction, action, be);
			}));
		}
		unlimitExecutor.submit(taskDecorator.decorate(() -> {
			notifyOwner(scheduleAction, action, e);
		}));
	}

	private void notifyAuthor(ScheduledAction scheduleAction, BotAction action, BotException e) {
		String authorId = scheduleAction.getAuthorId();
		jda.retrieveUserById(authorId).queue(tracer.trace((user) -> {
			Reply reply = createReplyForAuthor(scheduleAction, action, e);
			errorHandler.notifyUser(user, reply);
			
		}), tracer.trace((error) -> {
			log.error("Failed to notify author ", errorHandler);
		}));

	}

	private Reply createReplyForAuthor(ScheduledAction scheduleAction, BotAction action, BotException e) {
		return Reply.of().literal("Failed to execute Scheduled Action").newline() //
				.append(scheduleAction.shortReply()).newline() //
				.literal("With Error: ").append(e.getReplyMessage());//
	}

	private void notifyOwner(ScheduledAction scheduleAction, BotAction action, Throwable e) {
		errorHandler.notifyOwner(scheduleAction, e);
	}

	@Override
	public void destroy() throws Exception {
		executorService.shutdownNow();
	}

}
