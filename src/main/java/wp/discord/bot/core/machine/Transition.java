package wp.discord.bot.core.machine;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import wp.discord.bot.util.ToStringUtils;

/**
 * Route or Edge
 * 
 * @author PC
 *
 */
@Getter
@Setter(value = AccessLevel.PROTECTED)
public class Transition implements Acceptable<String> {

	private String value;
	private Type type;

	@JsonIgnore
	private transient State to;
	private String toNodeName;

	private List<StateChangeListener> listeners;

	@Override
	public boolean canAccept(String arg) {
		if (arg == null) {
			return false;
		}
		if (type == Type.EQUALS) {
			return value.equals(arg);

		} else if (type == Type.EQUALS_IGNORE_CASE) {
			return value.equalsIgnoreCase(arg);

		} else if (type == Type.PATTERN) {
			return arg.matches(value);

		} else if (type == Type.STARTS_WITH) {
			return arg.startsWith(value);

		} else if (type == Type.ENDS_WITH) {
			return arg.endsWith(value);
		}
		return value.equals(arg);
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
