package wp.discord.bot.task.get;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import wp.discord.bot.core.AudioTrackHolder;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;

@Component
public class GetAudioTask {

	@Autowired
	private AudioTrackHolder audioHolder;

	@Autowired
	private TracingHandler tracing;
	
	public void getAllAudio(BotAction action) throws Exception {
		List<AudioTrack> allTracks = audioHolder.getAllAudioTracks();

		Reply reply = Reply.of().bold("All Available Audio Name").newline();
		int count = 0;
		for (AudioTrack track : allTracks) {
			count++;
			reply.code(String.format("%2d) %s", count, audioHolder.getAudioTrackName(track))).newline();
		}
		reply.literal("To play audio, type: ").code("bot play audio [name]");

		tracing.queue(action.getEventMessageChannel().sendMessage(reply.build()));
	}

	
	// TODO make interactive reaction 
}
