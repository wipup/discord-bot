package wp.discord.bot.task.get;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Message;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.constant.Reaction;
import wp.discord.bot.core.AudioTrackHolder;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.task.audio.AudioListReference;
import wp.discord.bot.task.cron.CronEntity;
import wp.discord.bot.util.Reply;

@Component
public class GetAudioTask {

	@Autowired
	private AudioTrackHolder audioHolder;

	@Autowired
	private TracingHandler tracing;

	public void handleGetAudio(BotAction action) throws Exception {
		String name = action.getFirstTokenParam(CmdToken.NAME);

		Reply reply = Reply.of();
		if (StringUtils.isNotEmpty(name)) {
			Reply r = getAudiosByPatternReply(name, 0, AudioListReference.MAX_DISPLAY_SIZE);
			reply.append(r);
		} else {
			Reply r = getAllAudiosReply(0, AudioListReference.MAX_DISPLAY_SIZE);
			reply.append(r);
		}

		tracing.queue(action.getEventMessageChannel().sendMessage(reply.build()));
		action.getEventMessageChannel().sendMessage(reply.build()).queue(tracing.addTracingContext((m) -> {
			generateAudioListReaction(m, null);
		}));
	}

	public AudioListReference getAudios(BotAction action, AudioListReference ref) throws Exception {
		String name = ref.getNamePattern();
		int offset = ref.getOffset();
		int size = ref.getMaxDisplaySize();

		if (StringUtils.isNotEmpty(name)) {
			ref = getAudiosByPattern(name, offset, size);
		} else {
			ref = getAllAudio(offset, size);
		}

		return ref;
	}

	public void generateAudioListReaction(Message m, CronEntity cron) {
		m.addReaction(Reaction.OK.getCode()).queue();
		m.addReaction(Reaction.LEFT.getCode()).queue();
		m.addReaction(Reaction.RIGHT.getCode()).queue();
	}

	public Reply getAudiosByPatternReply(String pattern, int offset, int size) throws Exception {
		return getAudiosByPattern(pattern, offset, size).reply();
	}

	public AudioListReference getAudiosByPattern(String pattern, int offset, int size) throws Exception {
		AntPathMatcher matcher = new AntPathMatcher();

		List<AudioTrack> matchedTracks = audioHolder.getAllAudioTracks().stream() //
				.filter((track) -> matcher.match(pattern, track.getUserData().toString())) //
				.sorted((t1, t2) -> t1.getIdentifier().compareTo(t2.getIdentifier())) //
				.collect(Collectors.toList()); //

		if (CollectionUtils.isEmpty(matchedTracks)) {
			Reply reply = Reply.of().literal("Not found audio name matching ").code(pattern);
			throw new ActionFailException(reply);
		}

		int total = matchedTracks.size();

		AudioListReference ref = new AudioListReference();
		ref.setAudioTracks(matchedTracks.stream().skip(offset).limit(size).collect(Collectors.toList()));
		ref.setOffset(offset);
		ref.setNamePattern(pattern);
		ref.setTotal(total);
		return ref;
	}

	public Reply getAllAudiosReply(int offset, int size) throws Exception {
		return getAllAudio(offset, size).reply();
	}

	public AudioListReference getAllAudio(int offset, int size) throws Exception {
		List<AudioTrack> allTracks = audioHolder.getAllAudioTracks().stream() //
				.skip(offset).limit(size).collect(Collectors.toList());

		if (CollectionUtils.isEmpty(allTracks)) {
			Reply reply = Reply.of().literal("No audio available");
			throw new ActionFailException(reply);
		}

		AudioListReference ref = new AudioListReference();
		ref.setAudioTracks(allTracks);
		ref.setOffset(offset);
		ref.setTotal(audioHolder.getAllAudioTracks().size());
		return ref;
	}

}
