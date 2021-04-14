package wp.discord.bot.model.bot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.util.SafeUtil;

@Data
public class BotAction {

	private CmdAction action;
	private List<String> actionParams;

	private GenericEvent event;
	private Map<CmdEntity, String> entities;

	private String authorId;
	
	public BotAction() {
		actionParams = new ArrayList<>();
		entities = new LinkedHashMap<>();
	}

	public MessageReceivedEvent getMessageReceivedEvent() {
		return SafeUtil.get(() -> (MessageReceivedEvent) event);
	}
}
