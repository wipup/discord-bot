package wp.discord.bot.core.robot;

import lombok.Getter;
import lombok.Setter;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Setter
public class BotEvent implements CharSequence {

	private String name;
	private Object param;

	public static final BotEvent of(String name) {
		BotEvent e = new BotEvent();
		e.setName(name);
		return e;
	}

	public static final BotEvent of(String name, Object... params) {
		BotEvent e = of(name);
		e.setParam(params);
		return e;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof CharSequence)
			return obj.toString().equals(name);
		if (getClass() != obj.getClass())
			return false;
		BotEvent other = (BotEvent) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int length() {
		return name.length();
	}

	@Override
	public char charAt(int index) {
		return name.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return name.subSequence(end, end);
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
