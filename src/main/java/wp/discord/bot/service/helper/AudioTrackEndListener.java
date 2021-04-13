package wp.discord.bot.service.helper;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;

public interface AudioTrackEndListener extends AudioEventListener {

	@Override
	default void onEvent(AudioEvent event) {
		if (event instanceof TrackEndEvent) {
			onTrackEnd((TrackEndEvent) event);
		}
	};

	public void onTrackEnd(TrackEndEvent event);
}
