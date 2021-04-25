package wp.discord.bot.db.entity;

import java.time.Duration;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.support.CronTrigger;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.model.Describeable;
import wp.discord.bot.util.DateTimeUtil;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Slf4j
public class ScheduledOption implements Describeable {

	public static final String START_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String START_DATE_FORMAT_DISPLAY = "dd MMMM yyyy - HH:mm:ss.SSS";

	private ScheduledType type;
	private String value; // cron , or seconds
	private Date startTime;

	public ScheduledOption() {
	}

	public ScheduledOption(ScheduledType type, String value) {
		this.type = type;
		this.value = value;
	}

	// --------------------------------

	public static ScheduledOption cron(String cron) {
		return new ScheduledOption(ScheduledType.CRON, cron);
	}

	public static ScheduledOption fixedRate(String rate) {
		return fixedRate(rate, null);
	}

	public static ScheduledOption fixedRate(String rate, Date startTime) {
		ScheduledOption opt = new ScheduledOption(ScheduledType.FIXED_RATE, rate);
		if (startTime == null) {
			startTime = new Date();
		}
		opt.startTime = startTime;
		return opt;
	}

	public static ScheduledOption AtTime(Date d) {
		ScheduledOption opt = new ScheduledOption(ScheduledType.TIME, "");
		opt.startTime = d;
		return opt;
	}

	// --------------------------------

	public Date nextTriggerTime() {
		if (getType() == ScheduledType.CRON) {
			return DateTimeUtil.samplingDate(new CronTrigger(getValue()), 1).get(0);
		} else if (getType() == ScheduledType.FIXED_RATE) {
			return DateTimeUtil.addDurationToDate(Duration.parse(getValue()), getStartTime());
		} else {
			return null;
		}
	}

	public String nextTriggerTimeDisplay() {
		String nextRun = SafeUtil.get(() -> ToStringUtils.formatDate(nextTriggerTime(), START_DATE_FORMAT_DISPLAY));
		if (StringUtils.isBlank(nextRun)) {
			nextRun = " - ";
		}
		return nextRun;
	}

	@Override
	public Reply reply() {
		if (type == ScheduledType.CRON) {
			return replyCron();
		} else if (type == ScheduledType.FIXED_RATE) {
			return replyFixedDuration();
		} else {
			return replyFixedTime();
		}
	}

	private Reply getStartTimeDisplay() {
		try {
			return Reply.of().literal(" Start: ").code(ToStringUtils.formatDate(getStartTime(), START_DATE_FORMAT_DISPLAY));
		} catch (Exception e) {
			log.error("error printing getStartTimeDisplay: {}", this, e);
			return Reply.of().literal(" Start: ").code("Unable to read date");
		}
	}

	private Reply prettyPrintDurationValue() {
		try {
			Duration d = ToStringUtils.convertToDuration(getValue());
			return Reply.of().append(getStartTimeDisplay()).newline() //
					.literal("Repeat every: ").code(ToStringUtils.prettyPrintDurationValue(d));
		} catch (Exception e) {
			log.error("error printing duration: {}", this, e);
			return Reply.of().literal(" Start: ").code("Unable to read date");
		}
	}

	private Reply replyCron() {
		return Reply.of() //
				.literal(getType().getDisplayName()).literal(":  ").code(getValue()).newline() //
				.literal("Next Run: ").code(nextTriggerTimeDisplay()); //
	}

	private Reply replyFixedDuration() {
		return Reply.of() //
				.literal(getType().getDisplayName()).append(prettyPrintDurationValue()).newline() //
				.literal("Next Run: ").code(nextTriggerTimeDisplay()); //
	}

	private Reply replyFixedTime() {
		return Reply.of() //
				.literal(getType().getDisplayName()).append(getStartTimeDisplay());
	}

	@Override
	public String toString() {
		return ToStringUtils.toJsonString(this);
	}
}
