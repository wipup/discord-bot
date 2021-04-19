package wp.discord.bot.task.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wp.discord.bot.core.AudioTrackHolder;
import wp.discord.bot.core.EventErrorHandler;
import wp.discord.bot.core.RoleEnforcer;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;

@Component
public class UpdateAudioTrackTask {

	@Autowired
	private RoleEnforcer roleEnforcer;

	@Autowired
	private EventErrorHandler errorHandler;

	@Autowired
	private AudioTrackHolder trackHolder;

	@Autowired
	private TracingHandler tracing;

	public void reloadAllAudioTracks(BotAction action) throws Exception {
		roleEnforcer.allowOnlyOwner(action);

		try {
			trackHolder.reloadAudio();
		} catch (Exception e) {
			Reply r = Reply.of().literal("Reload Audio failed ").newline() //
					.append(errorHandler.createReply(e));
			throw new BotException(r, e);
		}

		Reply r = Reply.of().literal("Reload Audio completed").newline(); //
		tracing.queue(action.getEventMessageChannel().sendMessage(r.toString()));
	}

}
