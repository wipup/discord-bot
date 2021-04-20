package wp.discord.bot.core;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import wp.discord.bot.config.AsyncConfig;
import wp.discord.bot.core.bot.AbstractDiscordEventListener;
import wp.discord.bot.core.bot.BotSession;
import wp.discord.bot.core.bot.BotSessionManager;
import wp.discord.bot.core.persist.AbstractFileBasedRepository;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class ShutdownHandler implements DisposableBean {

	@Autowired
	private ConfigurableApplicationContext ctx;

	@Autowired
	private JDA jda;

	@Autowired
	private Collection<AbstractFileBasedRepository<?>> allRepository;

	@Autowired
	private BotSessionManager sessionManager;

	/**
	 * {@link AsyncConfig#BEAN_CRON_TASK_SCHEDULER}
	 */
	@Autowired
	private ScheduledActionManager scheduler;

	@Qualifier(AsyncConfig.BEAN_DATABASE_EXECUTOR)
	@Autowired
	private ExecutorService databaseThreadExecutor;

	@Qualifier(AsyncConfig.BEAN_UNLIMIT_EXECUTOR)
	@Autowired
	private ExecutorService genericEventExecutor;

	@Override
	public void destroy() throws Exception {
		stopListeners();

		allRepository.stream().forEach((r) -> {
			SafeUtil.suppress(() -> r.destroy());
		});

		destroyAllSession();
		destroyAllJDA();

		log.info("Closing DB Executor");
		waitAndShutdownExecutor(databaseThreadExecutor);

		log.info("Closing Generic Executor");
		waitAndShutdownExecutor(genericEventExecutor);

		shutdownScheduledTaskExecutors();
		log.info("Shutdown completed");
	}

	private void stopListeners() {
		jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.of(ActivityType.WATCHING, "Shuting Down"));
		SafeUtil.suppress(() -> Thread.sleep(50));
		ctx.getBeansOfType(AbstractDiscordEventListener.class).values().forEach((l) -> l.setReady(false));
	}

	public void shutdownScheduledTaskExecutors() {
		try {
			scheduler.destroy();
		} catch (Exception e) {
			log.error("error shutdown ScheduledActionManager", e);
		}
	}

	public void destroyAllSession() {
		log.info("Destroying all sessions");
		for (BotSession session : sessionManager.getAllSessions()) {
			try {
				log.info("Runnable Task left: {}", session.getExecutorService().shutdownNow().size());
			} catch (Exception e) {
				log.error("error shutdown BotSession: " + session, e);
			}
		}
	}

	private void waitAndShutdownExecutor(ExecutorService executor) throws Exception {
		databaseThreadExecutor.shutdown();
		databaseThreadExecutor.awaitTermination(10, TimeUnit.SECONDS);
	}

	public void destroyAllJDA() throws Exception {
		log.info("Shuting down JDA");
		shutdownJDA(jda);
	}

	public void shutdownJDA(JDA jda) {
		try {
			jda.shutdownNow();
		} catch (Exception e) {
			log.error("error shutdown JDA", e);
		}
	}

}
