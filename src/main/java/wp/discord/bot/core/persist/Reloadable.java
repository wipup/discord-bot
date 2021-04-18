package wp.discord.bot.core.persist;

import java.nio.file.Path;

public interface Reloadable<T> {

	public Class<T> getEntityClass();

	public String getRepositoryName();

	public T doRead(Path file) throws Exception;

	Path getPersistencePathDir() throws Exception;

	public void doReload(T entiy) throws Exception;

	default public String getFileExtension() {
		return ".json";
	};
}
