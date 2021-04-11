package wp.discord.bot.core.machine;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wp.discord.bot.util.ToStringUtils;

@Getter
@AllArgsConstructor
public class StateMachine {

	private State startNode = null;
	private Map<String, State> allStates = new HashMap<>(); // key = name, value = node

	public StateDriver newTraveler() {
		return new StateDriver(this);
	}
	
	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
