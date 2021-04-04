package wp.discord.bot.util;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ToStringUtils {

	public static String toString(Object obj) {
		if (obj == null) {
			return "null";
		}
		return ToStringBuilder.reflectionToString(obj, ToStringStyle.JSON_STYLE);
	}

}
