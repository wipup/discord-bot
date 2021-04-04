package wp.discord.bot.config.properties;

import java.util.List;

import wp.discord.bot.util.ToStringUtils;

public class RobotStateTransitionProperties {

	private String fromState;
	private List<String> fromStates;

	private String toState;
	private List<String> toStates;

	private String onEvent;
	private List<String> onEvents;

	private String callMethod;

	public String getFromState() {
		return fromState;
	}

	public void setFromState(String fromState) {
		this.fromState = fromState;
	}

	public String getToState() {
		return toState;
	}

	public void setToState(String toState) {
		this.toState = toState;
	}

	public String getOnEvent() {
		return onEvent;
	}

	public void setOnEvent(String onEvent) {
		this.onEvent = onEvent;
	}

	public String getCallMethod() {
		return callMethod;
	}

	public void setCallMethod(String callMethod) {
		this.callMethod = callMethod;
	}

	public List<String> getFromStates() {
		return fromStates;
	}

	public void setFromStates(List<String> fromStates) {
		this.fromStates = fromStates;
	}

	public List<String> getToStates() {
		return toStates;
	}

	public void setToStates(List<String> toStates) {
		this.toStates = toStates;
	}

	public List<String> getOnEvents() {
		return onEvents;
	}

	public void setOnEvents(List<String> onEvents) {
		this.onEvents = onEvents;
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
