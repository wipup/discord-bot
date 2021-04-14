package wp.discord.bot.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.constant.BotStatus;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.ActionHandler;
import wp.discord.bot.core.AudioTrackHolder;
import wp.discord.bot.core.BotSession;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.bot.BotAction;
import wp.discord.bot.util.Reply;

@Component
@Slf4j
public class PlayAudioTask implements ActionHandler {

	@Autowired
	private AudioTrackHolder audioHolder;

	@Autowired
	private JoinVoiceChannelTask joinChannelService;

	@Override
	public void handleAction(BotAction action) throws Exception {
		String trackName = action.getEntities().get(CmdEntity.AUDIO);
		AudioTrack track = audioHolder.getAudioTrack(trackName);
		if (track == null) {
			Reply reply = Reply.of().literal("Invalid audio ").code(trackName).newline() //
					.mentionUser(action.getAuthorId()).literal(" please re-check");
			throw new BotException(reply);
		}

		BotSession session = action.getSession();
		if (session.getStatus() == BotStatus.NOT_IN_VOICE_CHANNEL) {
			log.debug("join voice channel");
			joinChannelService.handleAction(action);
		}
		
		log.debug("join voice channel");
		session.playTrackAndLeaveChannel(track);
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.PLAY_AUDIO;
	}

}
