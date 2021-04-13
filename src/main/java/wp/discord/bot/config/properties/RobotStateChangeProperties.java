package wp.discord.bot.config.properties;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Setter
@Deprecated
public class RobotStateChangeProperties {

	private String state;
	
	private String callAction;
	private List<String> callActions;

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
