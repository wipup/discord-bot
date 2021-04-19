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
import wp.discord.bot.config.AsyncConfig;
import wp.discord.bot.core.action.ActionHandleManager;
import wp.discord.bot.core.cmd.CommandLineProcessor;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.db.entity.ScheduledOption;
import wp.discord.bot.db.entity.ScheduledType;
import wp.discord.bot.db.repository.ScheduleRepository;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class ScheduledActionManager implements DisposableBean {

	@Autowired
	@Qualifier(AsyncConfig.BEAN_CRON_TASK_DECORATOR)
	private TaskDecorator taskDecorator;

	@Autowired
	@Qualifier(AsyncConfig.BEAN_CRON_TASK_EXECUTOR)
	private ScheduledExecutorService executorService;

	@Autowired
	@Qualifier(AsyncConfig.BEAN_CRON_TASK_SCHEDULER)
	private TaskScheduler cronScheduler;

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
					try {
						BotAction action = cmdProcessor.handleCommand(null, cmd);
						if (action != null) {
							action.setAuthorId(scheduleAction.getAuthorId());
							action.setFromScheduler(true);
							actionManager.executeAction(action);
						}
					} catch (BotException e) {
						log.debug("Error executing schedule: {}", scheduleAction.getId(), e);
						notifyAuthor(e, scheduleAction);
					} catch (Throwable e) {
						log.debug("Fatal Error executing schedule: {}", scheduleAction.getId(), e);
						notifyOwner(e);
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
		unlimitExecutor.execute(() -> {
			log.debug("do cancel count-exceed task: {}", scheduleAction.getName());
			future.cancel(true);
			scheduleAction.setScheduledTask(null);
			scheduleAction.setActive(false);
			SafeUtil.suppress(() -> scheduleRepository.save(scheduleAction));
		});
		SafeUtil.suppress(() -> Thread.sleep(50));
	}

	private void notifyAuthor(BotException e, ScheduledAction scheduleAction) {
		errorHandler.notifyOwnerNow(null, e);
	}

	private void notifyOwner(Throwable e) {
		errorHandler.notifyOwnerNow(null, e);
	}

	@Override
	public void destroy() throws Exception {
		executorService.shutdownNow();
	}

}
