package wp.discord.bot.task.update;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wp.discord.bot.core.AudioTrackHolder;
import wp.discord.bot.core.EventErrorHandler;
import wp.discord.bot.core.RoleEnforcer;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;
import wp.discord.bot.util.ToStringUtils;

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

		long startTime = System.currentTimeMillis();
		try {
			List<Future<Void>> futureList = trackHolder.reloadAudio();
			futureList.stream().parallel().forEach((future) -> {
				SafeUtil.suppress(() -> future.get());
			});

		} catch (Exception e) {
			Reply r = Reply.of().literal("Reload Audio failed ").newline() //
					.append(errorHandler.createReply(e));
			throw new BotException(r, e);
		}

		long endTime = System.currentTimeMillis() - startTime;
		Duration duration = Duration.of(endTime, ChronoUnit.MILLIS);

		Reply r = Reply.of().literal("Reload Audio completed in ").code(ToStringUtils.prettyPrintDurationValue(duration)).newline() //
				.literal("Loaded: ").code(trackHolder.getAllAudioTracks().size()).literal(" Files");
		tracing.queue(action.getEventMessageChannel().sendMessage(r.toString()));
	}

}
