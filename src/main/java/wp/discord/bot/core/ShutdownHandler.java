package wp.discord.bot.core;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import wp.discord.bot.config.AsyncConfig;
import wp.discord.bot.core.bot.BotSession;
import wp.discord.bot.core.bot.BotSessionManager;
import wp.discord.bot.core.persist.AbstractFileBasedRepository;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class ShutdownHandler implements DisposableBean {

	@Autowired
	private Collection<JDA> jdas;

	@Autowired
	private Collection<AbstractFileBasedRepository<?>> allRepository;

	@Autowired
	private BotSessionManager sessionManager;

	@Autowired
	private ScheduledActionManager scheduler;

	@Qualifier(AsyncConfig.BEAN_GENERIC_EXECUTOR)
	@Autowired
	private ExecutorService genericSingleThreadExecutor;

	@Qualifier(AsyncConfig.BEAN_UNLIMIT_EXECUTOR)
	@Autowired
	private ExecutorService genericExecutor;

	@Override
	public void destroy() throws Exception {
		
		destroyAllSession();
		destroyAllJDA();
		shutdownSchedulerTasks();

		log.info("Closing Executor");
		SafeUtil.suppress(() -> log.info("Runnable Task left: {}", genericSingleThreadExecutor.shutdownNow().size()));
		SafeUtil.suppress(() -> log.info("Runnable Task left: {}", genericExecutor.shutdownNow().size()));

		allRepository.stream().forEach((r) -> {
			SafeUtil.suppress(() -> r.destroy());
		});
	}

	public void shutdownSchedulerTasks() {
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

	public void destroyAllJDA() throws Exception {
		log.info("Shuting down JDA");
		jdas.stream().forEach(this::shutdownJDA);
	}

	public void shutdownJDA(JDA jda) {
		try {
			jda.shutdownNow();
		} catch (Exception e) {
			log.error("error shutdown JDA", e);
		}
	}

}
