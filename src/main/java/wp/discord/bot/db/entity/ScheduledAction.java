package wp.discord.bot.db.entity;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import lombok.Data;
import lombok.ToString;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.model.Describeable;
import wp.discord.bot.model.Referenceable;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.ToStringUtils;

@Data
public class ScheduledAction implements Comparable<ScheduledAction>, Describeable, Referenceable {

	public static final CmdAction[] SCHEDULABLE_ACTIONS = new CmdAction[] { //
			CmdAction.JOIN_VOICE_CHANNEL, CmdAction.LEAVE_VOICE_CHANNEL, CmdAction.PLAY_AUDIO, //
			CmdAction.SEND_MESSAGE_TO_PRIVATE_CHANNEL, CmdAction.SEND_MESSAGE_TO_TEXT_CHANNEL, //
	};
	public static final int MAX_COMMAND_LINES = 20;
	public static final int MAX_NAME = 50;

	private BigInteger id;
	private String authorId;
	private String name;
	private String cron;
	private List<String> commands;

	@ToString.Exclude
	private transient ScheduledFuture<?> scheduledTask;

	@Override
	public Reply reply() {
		Reply rep = Reply.of() //
				.literal("ID:  ").code(String.format("%06d", getId())).literal("\t Owner: ").mentionUser(getAuthorId()).newline() //
				.literal("Name:  ").code(getName()).newline() //
				.literal("Cron:  ").code(getCron()).newline() //
				.startCodeBlock("bash");
		for (String cmd : getCommands()) {
			rep.literal(cmd).newline();
		}
		rep.endCodeBlock()//
				.literal("Active:  ").code(String.valueOf(isActive())).newline();
		return rep;
	}

	public Reply shortReply() {
		Reply rep = Reply.of() //
				.literal("ID: ").code(String.format("%2d ", getId())) //
				.literal(" Name: ").code(String.format("%s", getName())); //
		if (isActive()) {
			rep.bold(" (Active)");
		}
		return rep;
	}

	public boolean isActive() {
		return scheduledTask != null;
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}

	@Override
	public String entityID() {
		return getId().toString(16);
	}

	@Override
	public String entityName() {
		return CmdEntity.SCHEDULE.getCmd();
	}

	@Override
	public int compareTo(ScheduledAction o) {
		return this.id.compareTo(o.id);
	}
}
