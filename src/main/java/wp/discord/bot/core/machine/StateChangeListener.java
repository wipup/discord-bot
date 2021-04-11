package wp.discord.bot.core.machine;

public interface StateChangeListener {

	public void onStateChange(StateDriver driver, State from, State to, String value, Transition transition);

}
