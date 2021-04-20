package wp.discord.bot.core.persist;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.config.AsyncConfig;
import wp.discord.bot.config.ObjectMapperConfig;
import wp.discord.bot.config.properties.RepositoryProperties;
import wp.discord.bot.util.SafeUtil;

@Slf4j
public abstract class AbstractFileBasedRepository<T> implements Reloadable<T>, DisposableBean {

	@Autowired
	@Qualifier(ObjectMapperConfig.JSON_MAPPER)
	private ObjectMapper mapper;

	@Autowired
	private RepositoryProperties properties;

	@Qualifier(AsyncConfig.BEAN_DATABASE_EXECUTOR)
	@Autowired
	private ExecutorService executor;

	private Path persistencePath;

	@Override
	public Path getPersistencePathDir() throws Exception {
		if (persistencePath != null) {
			return persistencePath;
		}

		Path path = Paths.get(properties.getDirectoryPath()).resolve(getRepositoryName());
		if (!Files.isDirectory(path)) {
			path = Files.createDirectories(path);
		}
		persistencePath = path;
		return path;
	}

	abstract protected String getFileName(T entity);

	abstract protected Collection<T> getAllCachedEntities() throws Exception;

	protected void asyncUnpersist(T entity) throws Exception {
		executor.submit(() -> {
			SafeUtil.suppress(() -> {
				doUnpersist(entity);
			});
		});
	}

	protected void doUnpersist(T entity) throws Exception {
		Path path = getPersistencePathDir().resolve(getFileName(entity) + getFileExtension());
		log.debug("delete: {}", path);
		Files.deleteIfExists(path);
	}

	protected void asyncPersist(T entity) throws Exception {
		executor.submit(() -> {
			SafeUtil.suppress(() -> {
				doPersist(entity);
			});
		});
	}

	protected void doPersist(T entity) throws Exception {
		Path path = getPersistencePathDir().resolve(getFileName(entity) + getFileExtension());
		log.debug("writing: {}", path);
		byte[] bytes = mapper.writeValueAsBytes(entity);
		Files.write(path, bytes);
	}

	public T doRead(Path file) throws Exception {
		log.debug("reading: {}", file);
		byte[] bytes = Files.readAllBytes(file);
		return mapper.readValue(bytes, getEntityClass());
	}

	@Override
	public void destroy() throws Exception {
		getAllCachedEntities().stream().forEach((e) -> {
			try {
				doPersist(e);
			} catch (Exception ex) {
				log.error("error shutdown saving: {}", e, ex);
			}
		});
	}

	@Override
	public String getRepositoryName() {
		return this.getClass().getSimpleName();
	}
}
