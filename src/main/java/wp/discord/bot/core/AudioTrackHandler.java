package wp.discord.bot.core;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import wp.discord.bot.util.SafeUtil;

@Component
public class AudioTrackHandler {

	private Map<String, AudioTrack> audioTracks;

	public String getAudioTrackName(AudioTrack track) {
		Entry<String, AudioTrack> found = audioTracks.entrySet().stream() //
				.filter((t) -> t.getValue().getIdentifier().equals(track.getIdentifier())) //
				.findFirst().orElse(null);
		return SafeUtil.get(() -> found.getKey());
	}

	public AudioTrack getAudioTrack(String name) {
		AudioTrack track = audioTracks.get(name);
		if (track != null) {
			track = track.makeClone();
		}
		return track;
	}

	public void setAudioTracks(Map<String, AudioTrack> audioTracks) {
		this.audioTracks = audioTracks;
	}

}
