package wp.discord.bot.core;

import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.config.AsyncConfig;
import wp.discord.bot.core.action.ActionHandleManager;
import wp.discord.bot.core.cmd.CommandLineProcessor;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class ScheduledActionManager implements DisposableBean {

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

	public ScheduledFuture<?> scheduleCronTask(ScheduledAction scheduleAction) {
		CronTrigger cron = new CronTrigger(scheduleAction.getCron());

		ScheduledFuture<?> future = cronScheduler.schedule(newRunnableScheduledAction(scheduleAction), cron);
		scheduleAction.setScheduledTask(future);
		scheduleAction.setActive(true);
		return future;
	}

	private Runnable newRunnableScheduledAction(ScheduledAction scheduleAction) {
		log.debug("scheduling: {}", scheduleAction);
		return () -> {
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
					notifyAuthor(e, scheduleAction);
				} catch (Throwable e) {
					notifyOwner(e);
				}
			}
		};
	}

	private boolean isRunningCountExceed(ScheduledAction scheduleAction) {
		BigInteger actual = ObjectUtils.defaultIfNull(scheduleAction.getActualRunCount(), BigInteger.ZERO);
		BigInteger preferred = scheduleAction.getDesiredRunCount();
		if (preferred != null) {
//			if (actual >= preferred) {
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
		});
		SafeUtil.suppress(() -> Thread.sleep(50));
	}

	private void notifyAuthor(BotException e, ScheduledAction scheduleAction) {
//		errorHandler.notifyOwnerNow(null, e);
	}

	private void notifyOwner(Throwable e) {
		errorHandler.notifyOwnerNow(null, e);
	}

	@Override
	public void destroy() throws Exception {
		executorService.shutdownNow();
	}

}
