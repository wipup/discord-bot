package wp.discord.bot.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.core.action.ActionRouter;
import wp.discord.bot.locale.MessageKey;
import wp.discord.bot.locale.MessageLanguageResolver;
import wp.discord.bot.model.CommandContext;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class CommandProcessor implements ThreadContextAware {

	@Autowired
	private DiscordBotSessionManager bot;

	@Autowired
	private ActionRouter actionSelector;

	@Autowired
	private MessageLanguageResolver languageResolver;

	public void handleCommand(String command) throws Exception {
		String current = "";

		for (String fragment : command.split("\\s")) {
			if (StringUtils.isEmpty(fragment)) {
				continue;
			}

			current = current + fragment;

			if (bot.canAccept(current)) {
				bot.fireEvent(current);

				current = "";
//				log.debug("--------------------");
			} else {
				current += " ";
			}
		}

		if (StringUtils.isNotEmpty(current)) {
			// error
			log.debug("not accept: {}", current);
		}

		actionSelector.executeQueuedActions();
		sendReply();
		log.debug("=============================================================");
	}

	public void sendReply() {
		CommandContext ctx = getCurrentContext().getCommandContext();
		if (ctx == null) {
			return;
		}

		if (StringUtils.isNotEmpty(ctx.getReplyMessage())) {
			ctx.getMessageReceivedEvent().getChannel().sendMessage(ctx.getReplyMessage()).queue();
			return;
		}

		if (ctx.isActionDone()) {
			return;
		}
		
		String replyKey = SafeUtil.get(() -> ctx.getReplyMessageKey());
		if (StringUtils.isEmpty(replyKey)) {
			replyKey = MessageKey.REPLY_GREETING;
		}

		String replyMessage = languageResolver.getMessage(replyKey, ctx.getMessageReceivedEvent().getAuthor().getName());
		ctx.getMessageReceivedEvent().getChannel().sendMessage(replyMessage).queue();

	}
}
