package wp.discord.temp.model;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import wp.discord.bot.util.SafeUtil;
import wp.discord.bot.util.ToStringUtils;

@Getter
@Setter
public class CommandContext {

	private String action;
	private GenericEvent jdaEvent;
	private String actionValue;
	private boolean actionDone = false;
	private boolean actionError = false;

	private User targetUser;
	private AudioManager audioManager;
	private MessageChannel messageChannel;
	private VoiceChannel toVoiceChannel;
	private VoiceChannel authorVoiceChannel;

	private String replyMessage;
	private String replyMessageKey;

	public MessageReceivedEvent getMessageReceivedEvent() {
		return SafeUtil.get(() -> (MessageReceivedEvent) jdaEvent);
	}

	public void stopCallNextAction() {
		setActionError(true);
	}

	public User getAuthor() {
		return SafeUtil.get(() -> (User) jdaEvent.getClass().getMethod("getAuthor").invoke(jdaEvent));
	}

	public Guild getGuild() {
		return SafeUtil.get(() -> (Guild) jdaEvent.getClass().getMethod("getGuild").invoke(jdaEvent));
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}

}
