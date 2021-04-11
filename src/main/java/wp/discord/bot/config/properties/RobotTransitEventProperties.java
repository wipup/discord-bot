package wp.discord.bot.config.properties;

import java.util.regex.Pattern;

import org.apache.http.util.Asserts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wp.discord.bot.core.machine.Type;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RobotTransitEventProperties {

	private Type type;
	private String value;

	public Type getType() {
		if (type == Type.PATTERN) {
			Asserts.notNull(Pattern.compile(getValue()), "pattern value");
		}
		return type;
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
