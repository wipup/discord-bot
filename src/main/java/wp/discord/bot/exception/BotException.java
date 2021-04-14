package wp.discord.bot.exception;

import lombok.Getter;
import wp.discord.bot.util.Reply;

@Getter
public class BotException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Reply replyMessage;

	public BotException() {
		super();
	}

	public BotException(String message, Throwable cause) {
		this(Reply.of().literal(message), message, cause);
	}

	public BotException(String message) {
		this(Reply.of().literal(message), message);
	}

	public BotException(Throwable cause) {
		this((Reply) null, cause);
	}

	// --------

	public BotException(Reply reply) {
		this(reply, reply.toString(), null);
	}

	public BotException(Reply reply, String message, Throwable cause) {
		super(message, cause);
		this.replyMessage = reply;
	}

	public BotException(Reply reply, String message) {
		this(reply, message, null);
	}

	public BotException(Reply reply, Throwable cause) {
		this(reply, reply.toString(), cause);
	}

}
