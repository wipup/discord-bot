package wp.discord.bot.config.properties;

import wp.discord.bot.util.ToStringUtils;

public class RobotStateChangeProperties {

	private String state;

	private String callMethod;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCallMethod() {
		return callMethod;
	}

	public void setCallMethod(String callMethod) {
		this.callMethod = callMethod;
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
