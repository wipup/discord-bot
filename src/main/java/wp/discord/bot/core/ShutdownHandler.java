package wp.discord.bot.core;

import java.util.Collection;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import wp.discord.bot.core.bot.BotSession;
import wp.discord.bot.core.bot.BotSessionManager;

@Component
@Slf4j
public class ShutdownHandler implements DisposableBean {

	@Autowired
	private Collection<JDA> jdas;

	@Autowired
	private BotSessionManager sessionManager;

	@Override
	public void destroy() throws Exception {
		destroyAllSession();
		destroyAllJDA();
	}

	public void destroyAllSession() {
		log.info("Destroying all sessions");
		for (BotSession session : sessionManager.getAllSessions()) {
			session.getExecutorService().shutdownNow();
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
