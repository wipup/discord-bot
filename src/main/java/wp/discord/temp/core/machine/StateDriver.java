package wp.discord.temp.core.machine;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Slf4j
public class StateDriver implements Acceptable<String> {

	private StateMachine machine;
	private State currentState;
	private State previousState;
	
	@Deprecated
	private List<StateChangeListener> changeListeners;
	@Deprecated
	private List<StateNotChangeListener> notChangeListeners;

	public StateDriver(StateMachine graph) {
		reset(graph);
	}

	public void accept(String value) {
		Transition transition = findTransition(value);

		if (transition == null) {
			log.debug("Transition not accepted value: {}", value);
			callStateNotChangeListeners(currentState, value);
			return;
		}
		
		previousState = currentState;
		currentState = transition.getTo();

//		log.debug("Transition from: {}, to: {}, using: {}", previousState.getName(), currentState.getName(), value);
		log.debug("to-state: {}, using: {}", currentState.getName(), value);
		callStateChangeListeners(previousState, currentState, value, transition);
	}

	@Override
	public boolean canAccept(String value) {
		Transition r = findTransition(value);
		if (r == null) {
			return false;
		}
		return true;
	}

	private Transition findTransition(String value) {
		return currentState.findFirstTransition(value);
	}

	public void reset(StateMachine machine) {
		if (machine != null) {
			this.machine = machine;
		}
		reset();
	}

	public void reset() {
		currentState = machine.getStartNode();
		previousState = null;
	}

	public boolean isEnd() {
		return !currentState.hasMoreTransitionRules();
	}

	protected void callStateChangeListeners(State from, State to, String value, Transition transition) {
		if (CollectionUtils.isNotEmpty(transition.getListeners())) {
			transition.getListeners().stream().forEach((l) -> l.onStateChange(this, from, to, value, transition));
		}
		if (CollectionUtils.isNotEmpty(changeListeners)) {
			changeListeners.stream().forEach((l) -> l.onStateChange(this, from, to, value, transition));
		}
	}

	protected void callStateNotChangeListeners(State current, String value) {
		if (CollectionUtils.isNotEmpty(notChangeListeners)) {
			notChangeListeners.stream().forEach((l) -> l.onStateNotChange(this, current, value));
		}
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}

}
