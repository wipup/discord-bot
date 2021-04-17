package wp.discord.bot.config;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import brave.Span;
import wp.discord.bot.core.TracingHandler;

@EnableAsync
@Configuration
public class AsyncConfig {

	public static final String BEAN_CRON_TASK_EXECUTOR = "cronTaskExecutor";
	public static final String BEAN_CRON_TASK_SCHEDULER = "cronTaskScheduler";

	private static final AtomicInteger THREAD_COUNT = new AtomicInteger(0);

	@Autowired
	private TracingHandler tracing;

	@Bean(BEAN_CRON_TASK_EXECUTOR)
	public ScheduledExecutorService cronThreadExecutor() {
		final int TOTAL_THREAD = 1;
		return new ScheduledThreadPoolExecutor(TOTAL_THREAD, (r) -> new Thread(r, "cron-" + THREAD_COUNT.incrementAndGet()));
	}

	@Bean(BEAN_CRON_TASK_SCHEDULER)
	public TaskScheduler cronTaskScheduler(@Qualifier(BEAN_CRON_TASK_EXECUTOR) ScheduledExecutorService cronExecutor) {
		ConcurrentTaskScheduler cts = new ConcurrentTaskScheduler(cronExecutor);
		cts.setTaskDecorator(new TaskDecorator() {
			@Override
			public Runnable decorate(Runnable runnable) {
				return () -> {
					Span span = tracing.startNewTrace();
					try {
						runnable.run();
					} finally {
						tracing.clearTraceContext(span);
					}
				};
			}
		});
		return cts;
	}
}
