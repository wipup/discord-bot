package wp.discord.bot.core.machine;

public interface Acceptable<T> {

	public boolean canAccept(T value);
	
}
