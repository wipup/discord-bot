package wp.discord.temp.core.machine;

public interface StateNotChangeListener {

	public void onStateNotChange(StateDriver driver, State state, String value);

}
