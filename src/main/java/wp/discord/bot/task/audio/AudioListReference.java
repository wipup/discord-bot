package wp.discord.bot.task.audio;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import lombok.Data;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.model.Reference;
import wp.discord.bot.model.Referenceable;
import wp.discord.bot.util.Reply;

@Data
public class AudioListReference implements Referenceable {

	public static final int MAX_DISPLAY_SIZE = 20;

	@JsonIgnore
	private transient List<AudioTrack> audioTracks;

	private String namePattern;
	private Integer offset;
	private Integer total;

	public AudioListReference() {
		audioTracks = new ArrayList<>(0);
		namePattern = "";
		offset = 0;
		total = 0;
	}

	public static AudioListReference construct(Reference ref) {
		String id = ref.getId();
		String pattern = id.substring(id.indexOf("-") + 1, id.lastIndexOf("-"));

		String[] frags = id.split("-");
		String offset = frags[frags.length - 1];
		String total = frags[0];
		

		AudioListReference ent = new AudioListReference();
		ent.setNamePattern(pattern);
		ent.setOffset(Integer.parseInt(offset));
		ent.setTotal(Integer.parseInt(total));
		return ent;
	}

	@Override
	public Reply reply() {
		Reply reply = null;

		if (StringUtils.isNotEmpty(namePattern)) {
			reply = Reply.of().literal("Found ").code(total).literal(" Tracks").newline();
		} else {
			reply = Reply.of().bold("All Available Audio ").code(total).literal(" files").newline();
		}

		List<AudioTrack> displayTrack = new ArrayList<>(audioTracks);
		if (audioTracks.size() > MAX_DISPLAY_SIZE) {
			displayTrack = audioTracks.subList(offset, offset + MAX_DISPLAY_SIZE);
		}

		int count = offset;
		for (AudioTrack track : displayTrack) {
			count++;
			reply.code(String.format("%2d", count)).literal(") ").append(createReply(track)).newline();
		}
		return reply.append(createReplyHelper());
	}

	public Reply createReply(AudioTrack track) {
		return Reply.of().code(track.getUserData().toString());
	}

	public Reply createReplyHelper() {
		return Reply.of().literal("To play audio, type: ").code("bot play audio [name]").newline() //
				.literal("To filter audio, type: ").code("bot get audio name <pattern>");
	}

	@Override
	public String entityID() {
		return total + "-" + namePattern + "-" + offset;
	}

	@Override
	public CmdToken entityName() {
		return CmdToken.AUDIO;
	}

	public int getMaxDisplaySize() {
		return MAX_DISPLAY_SIZE;
	}

}
