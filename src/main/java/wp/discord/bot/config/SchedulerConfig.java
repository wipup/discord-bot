package wp.discord.bot.config;

import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@EnableScheduling
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

	@Autowired
	@Qualifier(AsyncConfig.BEAN_SINGLE_THREAD_EXECUTOR)
	private ExecutorService executor;

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		// https://www.baeldung.com/spring-scheduled-tasks
//		taskRegistrar.setScheduler(taskExecutor());
//        taskRegistrar.addTriggerTask(null, null);

	}

}
