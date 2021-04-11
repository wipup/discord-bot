package wp.discord.bot.core.machine;

public interface StateNotChangeListener {

	public void onStateNotChange(StateDriver driver, State state, String value);

}
