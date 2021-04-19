package wp.discord.bot.task;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;

@Component
public class GreetingActionHandler implements ActionHandler {

	@Override
	public void handleAction(BotAction action) throws Exception {
		MessageReceivedEvent event = action.getMessageReceivedEvent();

		Reply rep = Reply.of().literal("Hello ").mention(action.getEventAuthor()).newline() //
				.literal("To see how to use, please type ").code(" bot help ");
		event.getChannel().sendMessage(rep.build()).queue();
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.GREET;
	}

}
