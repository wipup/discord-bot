package wp.discord.bot.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageChannel;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;

@Component
@Slf4j
public class CompileCronTask implements ActionHandler {

	@Override
	public void handleAction(BotAction action) throws Exception {
		String cronExpr = StringUtils.join(action.getActionParams(), " ");
		CronTrigger cron = parse(action.getAuthorId(), cronExpr);

		log.debug("cron expression: {}", cronExpr);
		MessageChannel channel = action.getEventMessageChannel();
		if (channel == null) {
			return;
		}

		List<Date> sampleDates = samplingDate(cron, 3);
		Reply reply = Reply.of().literal("Cron Expression: ").code(cron.getExpression()).newline(); //

		int count = 0;
		for (Date date : sampleDates) {
			count++;
			reply.literal(count + ".) Date: ").code(DateFormatUtils.format(date, " dd MMMM yyyy ")) //
					.literal(" At time ").code(DateFormatUtils.format(date, " HH:mm:ss.SSS ")).newline();
		}

		channel.sendMessage(reply.build()).queue();
	}

	public CronTrigger parse(String authorId, String expr) throws Exception {
		try {
			return new CronTrigger(expr.trim());
		} catch (Exception e) {
			Reply reply = Reply.of().literal("Invalid CronExpression:  ").code(e.getMessage()).newline()//
					.mentionUser(authorId).literal(" please try again");
			throw new BotException(reply);
		}
	}

	private List<Date> samplingDate(CronTrigger cron, int count) {
		if (count <= 0) {
			return new ArrayList<>();
		}

		SimpleTriggerContext ctx = new SimpleTriggerContext();

		List<Date> result = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			Date d = cron.nextExecutionTime(ctx);
			if (d != null) {
				ctx.update(d, d, d);
				result.add(d);
				log.debug("Next trigger date: {}", DateFormatUtils.format(d, "dd MMMM yyyy - HH:mm:ss.SSS"));
			} else {
				log.trace("No more next trigger date");
			}
		}

		return result;
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.COMPILE_CRON;
	}

}
