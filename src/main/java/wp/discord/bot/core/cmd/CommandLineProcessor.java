package wp.discord.bot.core.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.bot.BotSessionManager;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.CommandLineTokenizer;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.EventUtil;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class CommandLineProcessor implements InitializingBean {

	@Autowired
	private JDA jda;

	@Autowired
	private BotSessionManager sessionManager;

	private Collection<CommandLineEntityReader> entityReaders;

	private Collection<CommandLineActionParameterReader> paramReaders;

	private List<Pattern> botInitCommands;

	public List<BotAction> handleMultiLineCommand(GenericEvent event, String multiLines) throws Exception {
		List<BotAction> result = new ArrayList<>();
		List<List<String>> multiCommands = CommandLineTokenizer.tokenizeMultiLines(multiLines);
		for (List<String> command : multiCommands) {
			BotAction action = handleTokenizedCommand(event, command.toArray(new String[command.size()]));
			if (result.isEmpty() && !command.isEmpty()) { // first non-empty line
				if (!isBotCommand(command)) {
					return result;
				}
			}
			if (action != null) {
				result.add(action);
			}
		}
		return result;
	}

	public BotAction handleCommand(GenericEvent event, String command) throws Exception {
		List<String> tokenizedCommand = CommandLineTokenizer.tokenize(command);
		return handleTokenizedCommand(event, tokenizedCommand.toArray(new String[tokenizedCommand.size()]));
	}

	public BotAction newBotAction(GenericEvent event) {
		BotAction action = new BotAction();
		action.setEvent(event);
		action.setAuthorId(EventUtil.getAuthorId(event));
		action.setSession(sessionManager.getBotSession(event));
		return action;
	}

	public BotAction handleTokenizedCommand(GenericEvent event, String[] commands) throws Exception {
		return handleTokenizedCommand(newBotAction(event), commands);
	}

	public BotAction handleTokenizedCommand(BotAction botAction, String[] commands) throws Exception {
		if (!isBotCommand(commands)) {
			return null;
		}

		log.debug("Received Command: {}", (Object) commands);
		BotAction action = botAction;
		try {
			String authorId = action.getAuthorId();

			for (int index = 1; index < commands.length; index++) {
				String frag = commands[index];
				if (StringUtils.isEmpty(frag)) {
					continue;
				}

				if (action.getAction() == null) {
					CmdAction cmdAction = CmdAction.getMatchingAction(frag);
					if (cmdAction == null) {
						Reply reply = Reply.of().literal("Unknown action: ").code(frag).newline() //
								.mentionUser(authorId).literal(" Please try again.");
						throw new BotException(reply);
					}
					action.setAction(cmdAction);

					index = collectActionParams(action, commands, index, cmdAction);
				} else {
					CmdEntity entity = CmdEntity.getMatchingEntity(frag);
					if (entity == null) {
						Reply reply = Reply.of().literal("Unknown option: ").code(frag).newline() //
								.mentionUser(authorId).literal(" Please try again.");
						throw new BotException(reply);
					}

					index = collectEntityOption(action, commands, index, entity);
				}
			}

			return action;
		} finally {
			log.debug("action: {}", action);
		}
	}

	private int collectActionParams(BotAction action, String[] tokens, int currentIndex, CmdAction cmdAction) {
		int count = 0;
		for (int i = currentIndex + 1; i < currentIndex + 1 + cmdAction.getParameterCount() && i < tokens.length; i++) {
			count++;
			String token = tokens[i];
			action.getActionParams().add(token);
		}
		return currentIndex + count;
	}

	private int collectEntityOption(BotAction action, String[] tokens, int currentIndex, CmdEntity entity) {
		int count = 0;
		for (int i = currentIndex + 1; i < currentIndex + 1 + entity.getParameterCount() && i < tokens.length; i++) {
			count++;
			action.getEntities(entity).add(tokens[i]);
		}
		return currentIndex + count;
	}

	// -------------------------------------

	private boolean isBotCommand(List<String> commands) {
		String firstWord = SafeUtil.get(() -> commands.get(0));
		return isBotCommand(firstWord);
	}

	private boolean isBotCommand(String[] commands) {
		String firstWord = SafeUtil.get(() -> commands[0]);
		return isBotCommand(firstWord);
	}

	private boolean isBotCommand(String s) {
		if (s == null) {
			return false;
		}
		return botInitCommands.stream().anyMatch((p) -> s.matches(p.pattern()));
	}

	// -------------------------------------

	@Override
	public void afterPropertiesSet() throws Exception {
		cacheBotMentionString();
	}

	private void cacheBotMentionString() {
		String botMentionString = DiscordFormat.mention(jda.getSelfUser());

		String[] botInitString = new String[] { //
				"bot", "/bot", "!bot", botMentionString //
		};

		botInitCommands = new ArrayList<>();
		for (String s : botInitString) {
			botInitCommands.add(Pattern.compile(Pattern.quote(s)));
		}
	}

}
