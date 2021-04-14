package wp.discord.bot.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.bot.BotSession;
import wp.discord.bot.util.SafeUtil;

@Getter
@Setter
@ToString
@Slf4j
public class BotAction {

	private CmdAction action;
	private List<String> actionParams;

	private GenericEvent event;
	private Map<CmdEntity, String> entities;

	private BotSession session;
	private String authorId;

	public BotAction() {
		actionParams = new ArrayList<>();
		entities = new LinkedHashMap<>();
	}

	public User getEventAuthor() {
		return SafeUtil.get(() -> (User) event.getClass().getMethod("getAuthor").invoke(event));
	}

	public MessageChannel getEventMessageChannel() {
		return SafeUtil.get(() -> (MessageChannel) event.getClass().getMethod("getChannel").invoke(event));
	}

	public MessageReceivedEvent getMessageReceivedEvent() {
		return SafeUtil.get(() -> (MessageReceivedEvent) event);
	}

	public void setSession(BotSession session) {
		Objects.requireNonNull(session);
		this.session = session;
	}

	public BotSession getSession() {
		if (session == null) {
			log.debug("sesion is null");
		}
		return session;
	}

	public String getAuthorId() {
		return SafeUtil.firstNonNull( //
				() -> getEventAuthor().getId(), //
				() -> authorId);
	}
}
