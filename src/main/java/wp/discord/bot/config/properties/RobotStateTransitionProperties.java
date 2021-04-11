package wp.discord.bot.config.properties;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Setter
public class RobotStateTransitionProperties {

	private String fromState;
	private List<String> fromStates;

	private String toState;
	private List<String> toStates;

	private String onString;
	private List<String> onStrings;

	private String callAction;

	private String paramName;

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
