package wp.discord.temp.core;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.events.GenericEvent;
import wp.discord.bot.core.BotSession;
import wp.discord.temp.core.machine.StateDriver;
import wp.discord.temp.core.machine.StateMachine;
import wp.discord.temp.model.CommandContext;

@Component
@Slf4j
public class DiscordBotSessionManager implements ThreadContextAware, InitializingBean {

//	@Autowired
//	private MessageLanguageResolver languageResolver;

	private BotSession nonGuildSession;
	private Map<String, BotSession> guildSessions;
	
	@Autowired
	private StateMachine stateMachine;

	@Autowired
	private JDA jda;

	public void newDriver(GenericEvent jdaEvent) {
		StateDriver driver = new StateDriver(stateMachine);
		getCurrentContext().setDriver(driver);
		getCurrentContext().setCommandContext(new CommandContext());
		getCurrentContext().getCommandContext().setJdaEvent(jdaEvent);
	}

	public boolean canAccept(String event) {
		return getCurrentContext().getDriver().canAccept(event);
	}

	public void fireEvent(String event) {
		getCurrentContext().getDriver().accept(event);
	}

	public boolean isMentioned(Message message) {
		if (message.isMentioned(jda.getSelfUser(), MentionType.USER)) {
			log.trace("mentioned by: {}", jda.getSelfUser());
			return true;
		}
		return false;
	}

	public JDA getJda() {
		return jda;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
	}

//	@Deprecated
//	public String getRootCommand(String message) {
//		String msg = StringUtils.trimToEmpty(message).toLowerCase();
//		msg = removeStartingSign(msg);
//		log.trace("message: {}", msg);
//
//		if (languageResolver.matchesKey(MessageKey.CALL_BOT, msg)) {
//			return MessageKey.CALL_BOT;
//		}
//
//		return null;
//	}
//
//	@Deprecated
//	public String getRootCommand(Message message) {
//		return getRootCommand(message.getContentDisplay());
//	}
//
//	@Deprecated
//	private String removeStartingSign(String msg) {
//		if (msg.startsWith("/") || msg.startsWith("\\") || msg.startsWith("-") || msg.startsWith("@")) {
//			return msg.replaceAll("\\W", "");
//		}
//		return msg;
//	}

}
