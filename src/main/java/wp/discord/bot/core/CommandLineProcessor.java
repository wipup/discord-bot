package wp.discord.bot.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.bot.BotAction;
import wp.discord.bot.service.helper.ReplyHelper;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class CommandLineProcessor implements InitializingBean {

	@Autowired
	private JDA jda;

	@Autowired
	private ActionHandleManager actionManager;

	@Autowired
	private ReplyHelper replyHelper;

	private List<Pattern> botInitCommands;

	public void handleCommand(MessageReceivedEvent event, String command) throws Exception {
		List<String> tokenizedCommand = CommandTokenizer.tokenize(command);
		handleCommand(event, tokenizedCommand.toArray(new String[tokenizedCommand.size()]));
	}

	public void handleCommand(MessageReceivedEvent event, String[] commands) throws Exception {
		handleCommand(event.getAuthor().getId(), event, commands);
	}

	public void handleCommand(String authorId, GenericEvent event, String[] commands) throws Exception {
		String firstWord = SafeUtil.get(() -> commands[0]);
		if (!isBotCommand(firstWord)) {
			return;
		}
		log.debug("Received Command: {}", (Object) commands);

		BotAction action = new BotAction();
		action.setEvent(event);
		action.setAuthorId(authorId);

		for (int index = 1; index < commands.length; index++) {
			String frag = commands[index];
			if (StringUtils.isEmpty(frag)) {
				continue;
			}

			if (action.getAction() == null) {
				CmdAction cmdAction = CmdAction.getMatchingAction(frag);
				if (cmdAction == null) {
					Reply reply = replyHelper.literal("Unknown action: ").code(frag).newline() //
							.mentionUser(authorId).literal(" Please try again.");
					throw new BotException(reply);
				}
				action.setAction(cmdAction);

				index = collectActionParams(action, commands, index, cmdAction);
			} else {
				CmdEntity entity = CmdEntity.getMatchingEntity(frag);
				if (entity == null) {
					Reply reply = replyHelper.literal("Unknown option: ").code(frag).newline() //
							.mentionUser(authorId).literal(" Please try again.");
					throw new BotException(reply);
				}

				action.getEntities().put(entity, frag);
				index = collectEntityOption(action, commands, index, entity);
			}
		}

		log.debug("action: {}", action);
		actionManager.executeAction(action);
	}

	private int collectActionParams(BotAction action, String[] tokens, int currentIndex, CmdAction cmdAction) {
		for (int i = currentIndex + 1; i < currentIndex + 1 + cmdAction.getParameterCount() && i < tokens.length; i++) {
			String token = tokens[i];
			action.getActionParams().add(token);
		}
		return currentIndex + cmdAction.getParameterCount();
	}

	private int collectEntityOption(BotAction action, String[] tokens, int currentIndex, CmdEntity entity) {
		for (int i = currentIndex + 1; i < currentIndex + 1 + entity.getParameterCount() && i < tokens.length; i++) {
			String token = tokens[i];
			action.getEntities().put(entity, token);
		}
		return currentIndex + entity.getParameterCount();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		String botMentionString = DiscordFormat.mention(jda.getSelfUser());

		String[] botInitString = new String[] { //
				"bot", "/bot", "!bot", botMentionString //
		};

		botInitCommands = new ArrayList<>();
		for (String s : botInitString) {
			botInitCommands.add(Pattern.compile(Pattern.quote(s)));
		}
	}

	private boolean isBotCommand(String s) {
		if (s == null) {
			return false;
		}
		return botInitCommands.stream().anyMatch((p) -> s.matches(p.pattern()));
	}

}
