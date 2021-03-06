package wp.discord.bot.core.cmd;

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
import wp.discord.bot.constant.BotReferenceConstant;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.core.bot.BotSessionManager;
import wp.discord.bot.exception.ActionFailException;
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

	// TODO
//	private Collection<CommandLineEntityReader> entityReaders;
//
//	private Collection<CommandLineActionParameterReader> paramReaders;

	private List<Pattern> botInitCommands;

	public List<BotAction> parseMultiLineCommand(GenericEvent event, String multiLines) throws Exception {
		List<BotAction> result = new ArrayList<>();
		List<List<String>> multiCommands = CommandLineTokenizer.tokenizeMultiLines(multiLines);
		for (List<String> command : multiCommands) {
			BotAction action = parseTokenizedCommand(event, command.toArray(new String[command.size()]));
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

	public BotAction parseCommand(GenericEvent event, String command) throws Exception {
		List<String> tokenizedCommand = CommandLineTokenizer.tokenize(command);
		return parseTokenizedCommand(event, tokenizedCommand.toArray(new String[tokenizedCommand.size()]));
	}

	public BotAction newBotAction(GenericEvent event) {
		BotAction action = new BotAction();
		action.setEvent(event);
		action.setAuthorId(EventUtil.getAuthorId(event));
		action.setSession(sessionManager.getBotSession(event));
		return action;
	}

	public BotAction parseTokenizedCommand(GenericEvent event, String[] commands) throws Exception {
		return parseTokenizedCommand(newBotAction(event), commands);
	}

	public BotAction parseTokenizedCommand(BotAction botAction, String[] commands) throws Exception {
		if (!isBotCommand(commands)) {
			return null;
		}

		log.debug("Received Command: {}", (Object) commands);
		BotAction action = botAction;
		try {
			String authorId = action.getAuthorId();

			for (int index = 1; index < commands.length; index++) {
				String token = commands[index];
				if (StringUtils.isEmpty(token)) {
					continue;
				}

				if (action.getAction() == null) {
					CmdAction cmdAction = CmdAction.getMatchingAction(token);
					if (cmdAction == null) {
						Reply reply = Reply.of().literal("Unknown action: ").code(token).newline() //
								.mentionUser(authorId).literal(" Please try again.");
						throw new ActionFailException(reply);
					}
					action.setAction(cmdAction);

					index = collectActionParams(action, commands, index, cmdAction);
				} else {
					CmdToken entity = CmdToken.getMatchingCmdToken(token);
					if (entity == null) {
						Reply reply = Reply.of().literal("Unknown option: ").code(token).newline() //
								.mentionUser(authorId).literal(" Please try again.");
						throw new ActionFailException(reply);
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

	private int collectEntityOption(BotAction action, String[] tokens, int currentIndex, CmdToken entity) {
		int count = 0;
		for (int i = currentIndex + 1; i < currentIndex + 1 + entity.getParameterCount() && i < tokens.length; i++) {
			count++;
			action.getAllTokenParams(entity).add(tokens[i]);
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
				BotReferenceConstant.BOT, "/" + BotReferenceConstant.BOT, "!" + BotReferenceConstant.BOT, botMentionString //
		};

		botInitCommands = new ArrayList<>();
		for (String s : botInitString) {
			botInitCommands.add(Pattern.compile(Pattern.quote(s)));
		}
	}

}
