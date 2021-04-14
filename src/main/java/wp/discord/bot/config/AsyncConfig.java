package wp.discord.bot.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration
public class AsyncConfig {

	public static final String BEAN_SINGLE_THREAD_EXECUTOR = "singleThreadExecutor";
	
	@Bean(BEAN_SINGLE_THREAD_EXECUTOR)
	@Primary
	public ExecutorService taskExecutor() {
		return Executors.newSingleThreadScheduledExecutor();
	}

}
