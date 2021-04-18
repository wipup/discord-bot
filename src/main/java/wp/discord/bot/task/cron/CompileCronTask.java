package wp.discord.bot.task.cron;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.Reaction;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.core.cmd.EntityReferenceHandler;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;

@Component
@Slf4j
public class CompileCronTask implements ActionHandler {

	@Autowired
	private EntityReferenceHandler refHandler;

	@Override
	public void handleAction(BotAction action) throws Exception {
		String cronExpr = StringUtils.join(action.getActionParams(), " ");
		CronEntity cron = parse(action.getAuthorId(), cronExpr);

		log.debug("cron expression: {}", cronExpr);
		MessageChannel channel = action.getEventMessageChannel();
		if (channel == null) {
			return;
		}

		replyCompiledCron(channel, cron);
	}

	public Reply createReplyCron(CronEntity cron) {
		return Reply.of().bold("Parse Success ").append(refHandler.generateEncodedReferenceCode(cron)).newline() //
				.append(cron.reply());
	}

	public void replyCompiledCron(MessageChannel channel, CronEntity cron) {
		Reply reply = createReplyCron(cron);
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

	public CronEntity parse(String authorId, String expr) throws Exception {
		try {
			return new CronEntity(expr);
		} catch (Exception e) {
			Reply reply = Reply.of().literal("Invalid CronExpression:  ").code(e.getMessage()).newline()//
					.mentionUser(authorId).literal(" please try again");
			throw new BotException(reply);
		}
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.COMPILE_CRON;
	}

}
