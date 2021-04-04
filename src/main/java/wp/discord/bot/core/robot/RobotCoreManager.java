package wp.discord.bot.core.robot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import wp.discord.bot.config.properties.RobotStateProperties;

@Component
public class RobotCoreManager {

	@Autowired
	private JDA jda;

	@Autowired
	private UntypedStateMachineBuilder robotBuilder;

	@Autowired
	private RobotStateProperties properties;

	@Autowired
	private RobotHelper robotHelper;

	public <T extends GenericEvent> RobotCore newRobot(T event) {
		RobotCore machine = newRobot();
		machine.setEventClass(event.getClass());
		machine.setJdaEvent(event);
		return machine;
	}

	public RobotCore newRobot() {
		RobotCore machine = (RobotCore) robotBuilder.newStateMachine(properties.getStartState());
		machine.setJda(jda);
		machine.setHelper(robotHelper);
		return machine;
	}

	public String getFinishEvent() {
		return properties.getFinishEvent();
	}

	public String getStartEvent() {
		return properties.getStartEvent();
	}

	public String getTerminateEvent() {
		return properties.getTerminateEvent();
	}

}
