package wp.discord.temp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.util.SafeUtil;
import wp.discord.temp.core.action.Action;
import wp.discord.temp.core.action.ActionConstant;
import wp.discord.temp.core.action.ActionExecutor;
import wp.discord.temp.locale.MessageKey;
import wp.discord.temp.locale.MessageLanguageResolver;
import wp.discord.temp.model.CommandContext;

@Component
@Slf4j
@ActionExecutor
public class BasicActionService {

	@Autowired
	private MessageLanguageResolver languageResolver;

	@Autowired
	private JDA jda;

	@Action(ActionConstant.ACTION_GREET_AUTHOR)
	public void greetAuthor(CommandContext context) {
		GenericEvent event = context.getJdaEvent();

		if (event instanceof MessageReceivedEvent) {
			MessageReceivedEvent e = (MessageReceivedEvent) event;

			String userName = SafeUtil.get(() -> e.getAuthor().getName(), "");
			String greetSentence = languageResolver.getMessage(MessageKey.REPLY_GREETING, userName);
			e.getChannel().sendMessage(greetSentence).queue();
		}
	}

	@Action(ActionConstant.ACTION_LOG_OUT)
	public void shutdown(CommandContext context) {
		MessageReceivedEvent event = (MessageReceivedEvent) context.getJdaEvent();
		log.debug("shutting down from user: {}", event.getAuthor());
		jda.shutdownNow();
	}

}
