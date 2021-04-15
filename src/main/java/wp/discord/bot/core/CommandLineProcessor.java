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
import wp.discord.bot.core.bot.BotSessionManager;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.task.helper.ReplyHelper;
import wp.discord.bot.util.CommandLineTokenizer;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class CommandLineProcessor implements InitializingBean {

	@Autowired
	private JDA jda;

	@Autowired
	private BotSessionManager sessionManager;

	@Autowired
	private ReplyHelper replyHelper;

	private List<Pattern> botInitCommands;

	public List<BotAction> handleMultiLineCommand(MessageReceivedEvent event, String multiLines) throws Exception {
		List<BotAction> result = new ArrayList<>();
		List<List<String>> multiCommands = CommandLineTokenizer.tokenizeMultiLines(multiLines);
		for (List<String> command : multiCommands) {
			BotAction action = handleTokenizedCommand(event, command.toArray(new String[command.size()]));
			if (action != null) {
				result.add(action);
			}
		}
		return result;
	}

	public BotAction handleCommand(MessageReceivedEvent event, String command) throws Exception {
		List<String> tokenizedCommand = CommandLineTokenizer.tokenize(command);
		return handleTokenizedCommand(event, tokenizedCommand.toArray(new String[tokenizedCommand.size()]));
	}

	public BotAction handleTokenizedCommand(MessageReceivedEvent event, String[] commands) throws Exception {
		return handleTokenizedCommand(event.getAuthor().getId(), event, commands);
	}

	public BotAction handleTokenizedCommand(String authorId, GenericEvent event, String[] commands) throws Exception {
		String firstWord = SafeUtil.get(() -> commands[0]);
		if (!isBotCommand(firstWord)) {
			return null;
		}
		
		log.debug("Received Command: {}", (Object) commands);
		BotAction action = null;
		try {
			action = new BotAction();
			action.setEvent(event);
			action.setAuthorId(authorId);
			action.setSession(sessionManager.getBotSession(event));

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
