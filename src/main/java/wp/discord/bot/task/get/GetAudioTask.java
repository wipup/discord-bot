package wp.discord.bot.task.get;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.core.AudioTrackHolder;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;

@Component
public class GetAudioTask {

	public static final int MAX_DISPLAY_SIZE = 25;

	@Autowired
	private AudioTrackHolder audioHolder;

	@Autowired
	private TracingHandler tracing;

	public void handleGetAudio(BotAction action) throws Exception {
		String name = action.getFirstTokenParam(CmdToken.NAME);
		if (StringUtils.isNotEmpty(name)) {
			getAudiosByPattern(action, name);
			return;
		}

		getAllAudio(action);
	}

	public void getAudiosByPattern(BotAction action, String pattern) throws Exception {
		AntPathMatcher matcher = new AntPathMatcher();

		List<AudioTrack> matchedTracks = audioHolder.getAllAudioTracks().stream() //
				.filter((track) -> matcher.match(pattern, track.getIdentifier())) //
				.sorted((t1, t2) -> t1.getIdentifier().compareTo(t2.getIdentifier())) //
				.collect(Collectors.toList()); //

		if (CollectionUtils.isEmpty(matchedTracks)) {
			Reply reply = Reply.of().literal("Not found audio name matching ").code(pattern);
			throw new ActionFailException(reply);
		}

		Reply reply = Reply.of().literal("Found ").code(matchedTracks.size()).literal(" Tracks").newline();
		reply.append(createReply(matchedTracks));
		tracing.queue(action.getEventMessageChannel().sendMessage(reply.build()));
	}

	public void getAllAudio(BotAction action) throws Exception {
		List<AudioTrack> allTracks = audioHolder.getAllAudioTracks();

		int foundSize = allTracks.size();
		if (allTracks.size() > MAX_DISPLAY_SIZE) {
			allTracks = allTracks.subList(0, MAX_DISPLAY_SIZE);
		}

		Reply reply = Reply.of().bold("All Available Audio ").code(foundSize).literal(" files").newline();
		reply.append(createReply(allTracks)).newline().append("To filter audio, type: ").code("bot get audio name <pattern>");
		tracing.queue(action.getEventMessageChannel().sendMessage(reply.build()));
	}

	public Reply createReply(List<AudioTrack> tracks) {
		Reply reply = Reply.of();
		int count = 0;
		for (AudioTrack track : tracks) {
			count++;
			reply.code(String.format("%2d", count)).literal(") ").append(createReply(track)).newline();
		}
		return reply.append(createReplyHelper());
	}

	public Reply createReply(AudioTrack track) {
		return Reply.of().code(audioHolder.getAudioTrackName(track));
	}

	public Reply createReplyHelper() {
		return Reply.of().literal("To play audio, type: ").code("bot play audio [name]").newline();
	}
}
