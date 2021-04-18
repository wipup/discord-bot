package wp.discord.bot.core.persist;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.config.ObjectMapperConfig;
import wp.discord.bot.config.properties.RepositoryProperties;
import wp.discord.bot.core.ScheduledActionManager;
import wp.discord.bot.db.repository.ScheduleRepository;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
@EnableConfigurationProperties(RepositoryProperties.class)
public class RepositoryReloader implements InitializingBean {

	@Autowired
	@Qualifier(ObjectMapperConfig.JSON_MAPPER)
	private ObjectMapper mapper;

	@Autowired
	private Collection<Reloadable<?>> reloadableList;

	@Autowired
	private ScheduledActionManager scheduledActionManager;

	@Autowired
	private ScheduleRepository scheduledRepository;

	@Override
	public void afterPropertiesSet() throws Exception {
		for (Reloadable<?> r : reloadableList) {
			log.debug("reloading: {}", r.getRepositoryName());
			reload(r);
		}
		rescheduledTask();
	}

	public <T> void reload(Reloadable<T> reloadable) throws Exception {
		Path dir = reloadable.getPersistencePathDir();
		try (Stream<Path> pathStream = Files.list(dir)) {
			pathStream.filter(Files::isRegularFile) //
					.filter(Files::isReadable) //
					.filter((p) -> p.getFileName().toString().endsWith(reloadable.getFileExtension())) //
					.map((p) -> SafeUtil.runtimeException(() -> reloadable.doRead(p))) //
					.filter((e) -> e != null) //
					.forEach((e) -> {
						log.debug("calling doReload: {} with: {}", reloadable, e);
						SafeUtil.runtimeException(() -> reloadable.doReload(e));
					});
		}
	}

	private void rescheduledTask() throws Exception {
		scheduledRepository.findAll().stream() //
				.filter((a) -> a.isActive()) //
				.forEach((a) -> {
					log.debug("reschedule: {}", a);
					ScheduledFuture<?> future = scheduledActionManager.scheduleCronTask(a);
					a.setScheduledTask(future);
				});
	}
}
