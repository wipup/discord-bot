package wp.discord.bot.exception;

import java.util.Objects;

import wp.discord.bot.util.Reply;

/**
 * For expected error Error message will be reply
 *
 */
public class ActionFailException extends BotException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ActionFailException(Reply reply) {
		super(Objects.requireNonNull(reply));
	}

}
