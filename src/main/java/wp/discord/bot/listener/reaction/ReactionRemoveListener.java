package wp.discord.bot.listener.reaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.bot.AbstractDiscordEventListener;
import wp.discord.bot.core.cmd.CommandLineProcessor;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.task.reaction.ReactionHandleTask;

@Component
public class ReactionRemoveListener extends AbstractDiscordEventListener<MessageReactionRemoveEvent> {

	@Autowired
	private CommandLineProcessor cmdProcessor;

	@Autowired	
	private ReactionHandleTask task;

	@Override
	public void handleEvent(MessageReactionRemoveEvent event) throws Exception {
		BotAction action = cmdProcessor.newBotAction(event);
		action.setAuthorId(event.getUserId());
		action.getEntities(CmdEntity.MESSAGE).add(event.getMessageId());
		action.getEntities(CmdEntity.REACTION).add(event.getReaction().getReactionEmote().getAsReactionCode());

		task.handleAction(event, action);
	}

	@Override
	public Class<MessageReactionRemoveEvent> eventClass() {
		return MessageReactionRemoveEvent.class;
	}

}
