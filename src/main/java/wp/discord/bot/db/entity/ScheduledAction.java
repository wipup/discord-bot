package wp.discord.bot.db.entity;

import java.math.BigInteger;
import java.util.List;

import lombok.Data;
import wp.discord.bot.model.Describable;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.ToStringUtils;

@Data
public class ScheduledAction implements Comparable<ScheduledAction>, Describable {

	private BigInteger id;
	private String authorId;
	private String name;
	private String cron;
	private List<String> commands;
	private boolean active = false;

	@Override
	public int compareTo(ScheduledAction o) {
		return this.id.compareTo(o.id);
	}

	@Override
	public Reply reply() {
		Reply rep = Reply.of() //
				.literal("ID: ").code(String.format("%06d", getId())).literal("\t Owner: ").mentionUser(getAuthorId()).newline() //
				.literal("Name: ").code(getName()).newline() //
				.literal("Cron:").code(getCron()).newline() //
				.startCodeBlock("bash");
		for (String cmd : getCommands()) {
			rep.literal(cmd).newline();
		}
		rep.endCodeBlock()//
				.literal("Active: ").code(String.valueOf(isActive())).newline();
		return rep;
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}

}
