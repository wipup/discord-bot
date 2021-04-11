package wp.discord.bot.config.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;

import lombok.Getter;
import lombok.Setter;
import wp.discord.bot.core.machine.Type;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Setter
public class RobotStateTransitionProperties {

	private String fromState;
	private List<String> fromStates;

	private String toState;
	private List<String> toStates;

	private List<String> onStrings;
	private List<RobotTransitEventProperties> onEvents;

	private String callAction;
	private List<String> callActions;

	public List<RobotTransitEventProperties> getOnEvents() {
		List<String> strings = ObjectUtils.getIfNull(getOnStrings(), () -> new ArrayList<>());
		List<RobotTransitEventProperties> events = ObjectUtils.getIfNull(onEvents, () -> new ArrayList<>());

		List<RobotTransitEventProperties> stringEvents = strings.stream() //
				.map((s) -> new RobotTransitEventProperties(Type.getDefaultType(), s))//
				.collect(Collectors.toList());
		events.addAll(stringEvents);

		return events;
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
