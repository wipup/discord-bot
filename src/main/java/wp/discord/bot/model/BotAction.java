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
import wp.discord.bot.util.EventUtil;
import wp.discord.bot.util.SafeUtil;

@Getter
@Setter
@ToString
@Slf4j
public class BotAction {

	private CmdAction action;
	private List<String> actionParams;

	private GenericEvent event;
	private Map<CmdEntity, List<String>> entities;

	private BotSession session;
	private String authorId;

	private boolean isFromScheduler;
	private boolean isFromTrigger;

	public BotAction() {
		actionParams = new ArrayList<>();
		entities = new LinkedHashMap<>();
		isFromTrigger = false;
		isFromScheduler = false;
	}

	public String getEntitiesParam(CmdEntity e, int index) {
		List<String> list = getEntities(e);
		if (index < 0 || index >= list.size()) {
			return null;
		}
		return list.get(index);
	}

//	public String getRequiredEntitiesParam(CmdEntity e, int index) throws Exception {
//		String value = getEntitiesParam(e, index);
//		if (StringUtils.isBlank(value)) {
//			Reply reply = Reply.of().bold("Error!").literal(" " + e.getCmd() + " must not be empty");
//			throw new ActionFailException(reply);
//		}
//		return value;
//	}

	public String getFirstEntitiesParam(CmdEntity e) {
		return getEntitiesParam(e, 0);
	}

//	public String getRequiredFirstEntitiesParam(CmdEntity e) throws Exception {
//		String value = getFirstEntitiesParam(e);
//		if (StringUtils.isBlank(value)) {
//			Reply reply = Reply.of().bold("Error!").literal(" " + e.getCmd() + " value must not be empty");
//			throw new ActionFailException(reply);
//		}
//		return value;
//	}

	public List<String> getEntities(CmdEntity e) {
		List<String> list = entities.get(e);
		if (list == null) {
			list = new ArrayList<>(e.getParameterCount());
			entities.put(e, list);
		}
		return list;
	}

	public User getEventAuthor() {
		return EventUtil.getAuthor(event);
	}

	public MessageChannel getEventMessageChannel() {
		return EventUtil.getChannel(event);
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
