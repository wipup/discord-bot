package wp.discord.bot.core.graph;

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
public class Route implements Acceptable<String> {

	private String value;
	private Type type;
	private int weight = 0;

	@JsonIgnore
	private transient Node to;
	private String toNodeName;

	@Override
	public boolean canAccept(String arg) {
		if (arg == null) {
			return false;
		}
		if (type == Type.EQUALS) {
			return value.equals(arg);

		} else if (type == Type.EQUAL_IGNORE_CASE) {
			return value.equalsIgnoreCase(arg);

		} else if (type == Type.PATTERN) {
			return arg.matches(value);

		} else if (type == Type.STARTS_WITH) {
			return arg.startsWith(value);

		} else if (type == Type.ENDS_WITH) {
			return arg.endsWith(value);
		}
		return false;
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
