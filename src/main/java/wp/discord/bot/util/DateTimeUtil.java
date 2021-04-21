package wp.discord.bot.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateTimeUtil {

	public static Date addDurationToDate(Duration duration, Date startDate) {
		Temporal t = duration.addTo(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault()));
		return Date.from(LocalDateTime.from(t).atZone(ZoneId.systemDefault()).toInstant());
	}

	public static List<Date> samplingDate(CronTrigger trigger, int count) {
		log.debug("sampling date for cron: {}", trigger);
		if (count <= 0) {
			return new ArrayList<>();
		}

		SimpleTriggerContext ctx = new SimpleTriggerContext();
		List<Date> result = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			Date d = trigger.nextExecutionTime(ctx);
			if (d != null) {
				ctx.update(d, d, d);
				result.add(d);
				log.trace("Next trigger date: {}", DateFormatUtils.format(d, "dd MMMM yyyy - HH:mm:ss.SSS"));
			} else {
				log.trace("No more next trigger date");
			}
		}

		return result.stream().distinct().sorted().collect(Collectors.toList());
	}

}
