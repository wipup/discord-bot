package wp.discord.bot.task.reaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import wp.discord.bot.constant.Reaction;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.model.Reference;
import wp.discord.bot.task.cron.CompileCronActionHandler;
import wp.discord.bot.task.cron.CronEntity;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class CompileCronReactionTask {

	@Autowired
	private TracingHandler tracing;
	
	@Autowired
	private CompileCronActionHandler cronTask;

	public void handleAction(GenericMessageReactionEvent event, BotAction action, Message message, Reference ref) throws Exception {
		ReactionEmote emote = event.getReactionEmote();

		Reaction reaction = Reaction.getReaction(emote);
		CronEntity cron = SafeUtil.get(() -> processReaction(CronEntity.construct(ref), reaction));

		if (cron == null) {
			return;
		}
		
		Reply reply = cronTask.createReplyCron(cron);
		tracing.queue(message.editMessage(reply.build()));
	}

	public CronEntity processReaction(CronEntity cron, Reaction reaction) {
		if (reaction == Reaction.LEFT) {
			cron.setCursor(cron.getCursor() - 1);
			
		} else if (reaction == Reaction.RIGHT) {
			cron.setCursor(cron.getCursor() + 1);
			
		} else if (reaction == Reaction.UP) {
			cron = cron.increase();
			
		} else if (reaction == Reaction.DOWN) {
			cron = cron.decrease();
			
		} else if (reaction == Reaction.NO_CHECKED) {
			cron.resetCursor();
		}
		return cron;
	}
}
