package wp.discord.bot.task.audio;

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

@Component
public class PlayAudioActionHandler implements ActionHandler {

	@Autowired
	private AudioTrackHolder audioHolder;

	@Autowired
	private JoinVoiceChannelActionHandler joinChannelTask;

	@Override
	public void handleAction(BotAction action) throws Exception {
		String trackName = action.getFirstTokenParam(CmdToken.AUDIO);
		AudioTrack track = audioHolder.getAudioTrack(trackName);
		if (track == null) {
			Reply reply = Reply.of().literal("Invalid audio-id ").code(trackName).newline() //
					.mentionUser(action.getAuthorId()).literal(" please re-check");
			throw new ActionFailException(reply);
		}

		BotSession session = action.getSession();
		joinChannelTask.handleAction(action);

		if (session != null) {
			session.playTrack(track);
			session.playTrack(track);
			session.leaveVoiceChannel();
			

		} else { // unknown session
			Reply reply = Reply.of().literal("Unknown voice-channel ").newline() //
					.mentionUser(action.getAuthorId()).literal(" please re-check");
			throw new ActionFailException(reply);
		}

	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.PLAY_AUDIO;
	}

}
