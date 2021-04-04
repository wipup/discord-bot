package wp.discord.bot.core;

import java.util.Collection;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;

@Component
@Slf4j
public class ShutdownHandler implements DisposableBean {

	@Autowired
	private Collection<JDA> jdas;

	@Override
	public void destroy() throws Exception {
		destroyAllJDA();
	}

	public void destroyAllJDA() throws Exception {
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
