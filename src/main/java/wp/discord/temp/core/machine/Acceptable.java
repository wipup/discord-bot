package wp.discord.temp.core.machine;

public interface Acceptable<T> {

	public boolean canAccept(T value);
	
}
