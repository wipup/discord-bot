package wp.discord.bot.listener.reaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.AbstractDiscordEventListener;
import wp.discord.bot.core.CommandLineProcessor;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.task.reaction.ReactionHandleTask;

@Component
public class ReactionAddListener extends AbstractDiscordEventListener<MessageReactionAddEvent> {

	@Autowired
	private CommandLineProcessor cmdProcessor;

	@Autowired
	private ReactionHandleTask task;

	@Override
	public void handleEvent(MessageReactionAddEvent event) throws Exception {

		BotAction action = cmdProcessor.newBotAction(event);
		action.setAuthorId(event.getUserId());
		action.getEntities(CmdEntity.MESSAGE).add(event.getMessageId());
		action.getEntities(CmdEntity.REACTION).add(event.getReaction().getReactionEmote().getAsReactionCode());

		task.handleAction(event, action);
	}

	@Override
	public Class<MessageReactionAddEvent> eventClass() {
		return MessageReactionAddEvent.class;
	}

}
