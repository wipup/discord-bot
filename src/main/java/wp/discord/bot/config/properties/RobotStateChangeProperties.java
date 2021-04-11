package wp.discord.bot.config.properties;

import lombok.Getter;
import lombok.Setter;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Setter
public class RobotStateChangeProperties {

	private String state;
	private String callAction;

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
