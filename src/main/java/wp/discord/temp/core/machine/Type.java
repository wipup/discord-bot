package wp.discord.temp.core.machine;

public enum Type {
	EQUALS, EQUALS_IGNORE_CASE, PATTERN, STARTS_WITH, ENDS_WITH, //
	MENTIONED;

	public static final Type getDefaultType() {
		return Type.EQUALS;
	}

}
