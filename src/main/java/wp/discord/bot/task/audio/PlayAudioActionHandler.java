package wp.discord.bot.task.audio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.core.AudioTrackHolder;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.core.bot.BotSession;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class PlayAudioActionHandler implements ActionHandler {

	@Autowired
	private AudioTrackHolder audioHolder;

	@Autowired
	private JoinVoiceChannelActionHandler joinChannelTask;

	@Override
	public void handleAction(BotAction action) throws Exception {
		List<AudioTrack> tracks = getAudioTracks(action);

		BotSession session = action.getSession();
		joinChannelTask.handleAction(action);

		int playCount = getCount(action);
		if (session != null) {
			playAudios(session, tracks, playCount);

		} else { // unknown session
			Reply reply = Reply.of().literal("Unknown voice-channel ").newline() //
					.mentionUser(action.getAuthorId()).literal(" please re-check");
			throw new ActionFailException(reply);
		}

	}

	public void playAudios(BotSession session, List<AudioTrack> tracks, int playCount) {
		for (int i = 0; i < playCount; i++) {
			for (AudioTrack track : tracks) {
				session.playTrack(track);
				session.playTrack(track);
			}
		}
		session.leaveVoiceChannel();
	}

	public List<AudioTrack> getAudioTracks(BotAction action) throws Exception {
		String names = action.getFirstTokenParam(CmdToken.AUDIO);
		if (StringUtils.isEmpty(names)) {
			Reply reply = Reply.of().literal("Audio track name cannot be empty").newline() //
					.literal("To see audio list, type: bot get audio");
			throw new ActionFailException(reply);
		}

		List<String> trackNames = Arrays.asList(names.split(","));
		List<AudioTrack> tracks = new ArrayList<>(trackNames.size());
		for (String n : trackNames) {
			AudioTrack track = getAudioTrack(action, n.trim());
			tracks.add(track);
		}

		if (CollectionUtils.isEmpty(tracks)) {
			Reply reply = Reply.of().literal("Invalid audio-id ").code(names).newline() //
					.mentionUser(action.getAuthorId()).literal(" please re-check");
			throw new ActionFailException(reply);
		}
		return tracks;
	}

	public AudioTrack getAudioTrack(BotAction action, String trackName) throws Exception {
		AudioTrack track = audioHolder.getAudioTrack(trackName);
		if (track == null) {
			Reply reply = Reply.of().literal("Invalid audio-id ").code(trackName).newline() //
					.mentionUser(action.getAuthorId()).literal(" please re-check");
			throw new ActionFailException(reply);
		}

		return track;
	}

	private int getCount(BotAction action) {
		String count = action.getFirstTokenParam(CmdToken.COUNT);
		Integer c = SafeUtil.get(() -> Integer.parseInt(count));
		if (c == null) {
			return 2;
		}
		return Math.abs(c);
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.PLAY_AUDIO;
	}

}
