package wp.discord.bot.task.cron;

import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.constant.Reaction;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.core.cmd.EntityReferenceHandler;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class CompileCronActionHandler implements ActionHandler {

	public static final BigInteger MAX_SAMPLING_COUNT = new BigInteger("20");
	public static final int DEFAULT_SAMPLING_COUNT = 3;

	@Autowired
	private EntityReferenceHandler refHandler;

	@Override
	public void handleAction(BotAction action) throws Exception {
		String cronExpr = StringUtils.join(action.getActionParams(), " ");
		CronEntity cron = parse(cronExpr);

		log.debug("cron expression: {}", cronExpr);
		MessageChannel channel = action.getEventMessageChannel();
		if (channel == null) {
			return;
		}

		int samplingCount = DEFAULT_SAMPLING_COUNT;
		String count = StringUtils.defaultString(action.getFirstTokenParam(CmdToken.COUNT));
		if (StringUtils.isNotBlank(count)) {
			BigInteger bi = SafeUtil.get(() -> new BigInteger(count));
			if (bi == null) {
				Reply r = Reply.of().literal("Invalid sampling count number: ").code(count);
				throw new ActionFailException(r);
			}
			if (bi.compareTo(BigInteger.ZERO) <= 0 || bi.compareTo(MAX_SAMPLING_COUNT) > 0) {
				Reply r = Reply.of().literal("Invalid sampling count number: ").code(count).newline() //
						.literal("sampling count must be between ").code(" 1 - 20 ");
				throw new ActionFailException(r);
			}
			samplingCount = bi.intValue();
		}

		replyCompiledCron(channel, cron, samplingCount);
	}

	public Reply createReplyCron(CronEntity cron) {
		return createReplyCron(cron, DEFAULT_SAMPLING_COUNT);
	}

	public Reply createReplyCron(CronEntity cron, int samplingCount) {
		return Reply.of().bold("Parse Success ").append(refHandler.generateEncodedReferenceCode(cron)).newline() //
				.append(cron.reply(samplingCount));
	}

	public void replyCompiledCron(MessageChannel channel, CronEntity cron, int samplingCount) {
		Reply reply = createReplyCron(cron, samplingCount);
		channel.sendMessage(reply.build()).queue((m) -> {
			generateCronEditorReply(m, cron);
		});
	}

	public void generateCronEditorReply(Message m, CronEntity cron) {
		m.addReaction(Reaction.LEFT.getCode()).queue();
		m.addReaction(Reaction.UP.getCode()).queue();
		m.addReaction(Reaction.DOWN.getCode()).queue();
		m.addReaction(Reaction.RIGHT.getCode()).queue();
		m.addReaction(Reaction.NO_CHECKED.getCode()).queue();
	}

	public CronEntity parse(String expr) throws Exception {
		try {
			return new CronEntity(expr);
		} catch (Exception e) {
			Reply reply = Reply.of().literal("Invalid CronExpression:  ").code(e.getMessage()).newline();
			throw new ActionFailException(reply);
		}
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.COMPILE_CRON;
	}

}
