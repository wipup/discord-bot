package wp.discord.bot.db.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.model.Describeable;
import wp.discord.bot.model.Reference;
import wp.discord.bot.model.Referenceable;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Getter
@Slf4j
@ToString
public class CronEntity implements Referenceable, Describeable {

	private final transient CronTrigger trigger;
	private final String expr;

	// controller state
	private transient int cursor = 0;
	private transient List<String> expressions;

	public CronEntity(String expr) {
		this.expr = expr.trim();
		this.trigger = new CronTrigger(this.expr);

		expressions = new ArrayList<>(6);
		CollectionUtils.addAll(expressions, expr.trim().split("\\s+"));
	}

	public List<Date> samplingDate(int count) {
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

		return result;
	}

	@Override
	public Reply reply() {
		List<Date> sampleDates = samplingDate(3);
		Reply reply = Reply.of().literal("Cron Expression: ");

		StringBuilder selectedExpr = new StringBuilder();
		int index = 0;
		for (String ex : getExpressions()) {
			index++;
			if (index == cursor) {
				selectedExpr.append("'");
				selectedExpr.append(ex);
				selectedExpr.append("'");
			} else {
				selectedExpr.append(ex);
			}
			selectedExpr.append("   ");
		}
		reply = reply.code(selectedExpr.toString());

		int count = 0;
		for (Date date : sampleDates) {
			count++;
			reply.newline().literal(count + ".) Date: ").code(DateFormatUtils.format(date, " dd MMMM yyyy ")) //
					.literal(" At time ").code(DateFormatUtils.format(date, " HH:mm:ss.SSS "));
		}

		return reply.newline();
	}

	public static CronEntity construct(Reference ref) {
		String uuid = ref.getId();
		int cursor = SafeUtil.get(() -> Integer.parseInt(uuid.substring(0, uuid.indexOf("-")).trim()), 0);
		String expr = uuid.substring(uuid.indexOf("-") + 1, uuid.length()).trim();

		CronEntity result = new CronEntity(expr);
		result.setCursor(cursor);
		return result;
	}

	public void setCursor(int cursor) {
		cursor = Math.min(cursor, 6);
		cursor = Math.max(cursor, 1);
		this.cursor = cursor;
	}

	public CronEntity updateExpressionValue(int delta) {
		int index = this.cursor - 1;
		index = Math.min(index, 5);
		index = Math.max(index, 0);

		final int finalIndex = index;
		String value = SafeUtil.get(() -> expressions.get(finalIndex));
		value = update(finalIndex, value, delta);

		List<String> newExpr = new ArrayList<>(expressions);
		newExpr.add(index, String.valueOf(value));
		newExpr.remove(index + 1);

		CronEntity newCron = new CronEntity(StringUtils.join(newExpr, " "));
		newCron.cursor = this.cursor;
		return newCron;
	}

	private String update(int index, String value, int delta) {
		Integer v = SafeUtil.get(() -> Integer.valueOf(value));
		if (v == null) {
			v = 0;
		}

		v = v + delta;
		if (index == 0 || index == 1) { // second , minute
			v = Math.min(v, 59);
			if (v < 0) {
				return "*";
			}
		} else if (index == 2) { // hour
			v = Math.min(v, 23);
			if (v < 0) {
				return "*";
			}
		} else if (index == 3) { // dom
			v = Math.min(v, 31);
			if (v < 1) {
				return "*";
			}
		} else if (index == 4) { // month
			v = Math.min(v, 12);
			if (v < 1) {
				return "*";
			}
		} else if (index == 5) { // dow
			v = Math.min(v, 7);
			if (v < 0) {
				return "*";
			}
		}

		return String.valueOf(v);
	}

	public CronEntity increase() {
		return updateExpressionValue(1);
	}

	public CronEntity decrease() {
		return updateExpressionValue(-1);
	}

	public void resetCursor() {
		this.cursor = 0;
	}

	@Override
	public String entityID() {
		return cursor + "-" + expr;
	}

	@Override
	public String entityName() {
		return CmdEntity.CRON.getCmd();
	}

	public String getExpression() {
		return trigger.getExpression();
	}

}
