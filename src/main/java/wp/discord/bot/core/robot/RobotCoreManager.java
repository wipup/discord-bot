package wp.discord.bot.core.robot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionBeginEvent;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionBeginListener;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionCompleteEvent;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionCompleteListener;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import wp.discord.bot.config.properties.RobotStateProperties;
import wp.discord.bot.util.ToStringUtils;

@Component
@Slf4j
public class RobotCoreManager implements //
		TransitionBeginListener<UntypedStateMachine, Object, Object, Object>, //
		TransitionCompleteListener<UntypedStateMachine, Object, Object, Object> //
{

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
		machine.setJda(robotHelper.getJda());
		machine.setHelper(robotHelper);
		machine.addTransitionBeginListener(this);
		machine.addTransitionCompleteListener(this);
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

	@Override
	public void transitionBegin(TransitionBeginEvent<UntypedStateMachine, Object, Object, Object> event) {
		log.debug("transitionBegin: {}", ToStringUtils.toString(event));
	}

	@Override
	public void transitionComplete(TransitionCompleteEvent<UntypedStateMachine, Object, Object, Object> event) {
		log.debug("transitionComplete: {}", ToStringUtils.toString(event));
	}

	public JDA getJda() {
		return robotHelper.getJda();
	}

}
