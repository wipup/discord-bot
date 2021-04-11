package wp.discord.bot.model;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.util.SafeUtil;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Setter
public class CommandContext {

	private String action;
	private GenericEvent jdaEvent;
	private Object actionParam;

	private MessageChannel messageChannel;
	private VoiceChannel toVoiceChannel;
	private VoiceChannel authorVoiceChannel;

	public MessageReceivedEvent getMessageReceivedEvent() {
		return SafeUtil.get(() -> (MessageReceivedEvent) jdaEvent);
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
