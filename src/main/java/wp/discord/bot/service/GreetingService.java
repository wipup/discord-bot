package wp.discord.bot.service;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.core.ActionHandler;
import wp.discord.bot.model.bot.BotAction;

@Component
public class GreetingService implements ActionHandler {

	@Override
	public void handleAction(BotAction action) throws Exception {
		MessageReceivedEvent event = action.getMessageReceivedEvent();
		event.getChannel().sendMessage("hello").queue();;
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.GREET;
	}

}
