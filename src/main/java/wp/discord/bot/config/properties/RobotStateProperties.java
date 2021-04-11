package wp.discord.bot.config.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import wp.discord.bot.util.ToStringUtils;

@ConfigurationProperties
@Data
public class RobotStateProperties {

	private String startState;
	private String startEvent;
	private String finalState;
	private String finishEvent;
	private String terminateEvent;

	private List<RobotStateTransitionProperties> transition;

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
