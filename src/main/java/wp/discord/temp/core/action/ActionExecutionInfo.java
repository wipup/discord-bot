package wp.discord.temp.core.action;

import java.util.List;

import lombok.Data;
import wp.discord.temp.core.machine.State;

@Data
public class ActionExecutionInfo {

	private String action;
	private State fromState;
	private State toState;
	private String eventValue;
	private List<ActionExecutionRoute> routes;
	
}
