package wp.discord.bot.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
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

	public static final String BEAN_CRON_TASK_DECORATOR = "taskDecorator";
	public static final String BEAN_CRON_TASK_EXECUTOR = "cronTaskExecutor";
	public static final String BEAN_CRON_TASK_SCHEDULER = "cronTaskScheduler";
	public static final String BEAN_DATABASE_EXECUTOR = "genericSingleThreadExecutor";
	public static final String BEAN_UNLIMIT_EXECUTOR = "genericUnlimitThreadExecutor";

	private static final AtomicInteger THREAD_COUNT = new AtomicInteger(0);

	@Autowired
	private TracingHandler tracing;

	@Bean(BEAN_DATABASE_EXECUTOR)
	public ExecutorService genericSingleThreadExecutor() {
		return Executors.newSingleThreadExecutor();
	}

	@Bean(BEAN_UNLIMIT_EXECUTOR)
	public ExecutorService genericUnlimitThreadExecutor() {
		return Executors.newCachedThreadPool();
	}

	@Bean(BEAN_CRON_TASK_EXECUTOR)
	public ScheduledExecutorService cronThreadExecutor() {
		final int TOTAL_THREAD = 1;
		return new ScheduledThreadPoolExecutor(TOTAL_THREAD, (r) -> new Thread(r, "scheduled-" + THREAD_COUNT.incrementAndGet()));
	}

	@Bean(BEAN_CRON_TASK_DECORATOR)
	public TaskDecorator decorator() {
		return (runnable) -> {
			return () -> {
				Span span = null;
				try {
					String traceId = tracing.getTraceId();
					if (StringUtils.isEmpty(traceId)) {
						span = tracing.startNewTrace();
					}
					runnable.run();
				} finally {
					if (span != null) {
						tracing.clearTraceContext(span);
					}
				}
			};
		};
	}

	@Bean(BEAN_CRON_TASK_SCHEDULER)
	public TaskScheduler cronTaskScheduler(@Qualifier(BEAN_CRON_TASK_EXECUTOR) ScheduledExecutorService cronExecutor, //
			@Qualifier(BEAN_CRON_TASK_DECORATOR) TaskDecorator decorator) {
		ConcurrentTaskScheduler cts = new ConcurrentTaskScheduler(cronExecutor);
		cts.setTaskDecorator(decorator);
		return cts;
	}
}
