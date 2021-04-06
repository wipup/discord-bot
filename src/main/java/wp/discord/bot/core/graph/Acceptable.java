package wp.discord.bot.core.graph;

public interface Acceptable<T> {

	public boolean canAccept(T value);
	
}
