package wp.discord.bot.util;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.boot.convert.DurationStyle;

public class ToStringUtils {

	public static String toJsonString(Object obj) {
		return toString(obj);
	}

	public static String toString(Object obj) {
		if (obj == null) {
			return "null";
		}
		return ToStringBuilder.reflectionToString(obj, ToStringStyle.JSON_STYLE);
	}

	public static String prettyPrintDurationValue(Duration duration) {
		String iso = duration.toString();
		iso = iso.replaceFirst("PT", "") //
				.replace("H", " Hours and ") //
				.replace("M", " Minutes and ")//
				.replace("S", " Seconds").trim();
		if (iso.endsWith("and")) {
			return iso.substring(0, iso.length() - "and".length()).trim();
		}
		return iso;
	}

	public static Date parseDate(String date, String pattern) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setLenient(false);
		return sdf.parse(StringUtils.trim(date));
	}

	public static String formatDate(Date date, String pattern) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setLenient(false);
		return sdf.format(date);
	}

	public static Duration convertToDuration(String str) {
		Duration d = SafeUtil.get(() -> Duration.parse(str));
		if (d != null) {
			return d;
		}
		return SafeUtil.get(() -> DurationStyle.detectAndParse(str));
	}
}
