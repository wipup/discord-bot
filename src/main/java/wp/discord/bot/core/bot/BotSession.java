package wp.discord.bot.core.bot;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.ToStringExclude;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStuckEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import wp.discord.bot.constant.BotStatus;

@Getter
@Setter
@ToString
@Slf4j
public class BotSession implements AudioSendHandler, AudioEventListener, ThreadFactory, EventListener {

	// internal-core
	@ToStringExclude
	private static final AtomicInteger seqGenerator = new AtomicInteger(0);

	@ToStringExclude
	private final ExecutorService executorService;
	private final JDA jda;

	private String guildId;
	private Guild guild;
	private BotStatus status;
	private AudioManager audioManager;

	// audio
	private AudioFrame lastFrame;
	private AudioPlayer audioPlayer;

	public BotSession(JDA jda) {
		this.jda = jda;
		this.executorService = Executors.newSingleThreadExecutor(this);
		this.jda.addEventListener(this);
	}

	// ---------- queue
	public void queue(Runnable r) {
		try {
			executorService.submit(r);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized void playTrack(AudioTrack track) {
		log.trace("queue playTrack");

		queue(() -> {
			log.trace("do playTrack");
			getAudioPlayer().playTrack(track.makeClone());
		});

		waitUntil("track start", (b) -> BotStatus.PLAYING_AUDIO == b.getStatus());
		waitUntil("track end", (b) -> BotStatus.VOICE_CHANNEL_IDLE == b.getStatus());
	}

	public synchronized void stopTrack() {
		log.trace("queue stopTrack");

		queue(() -> {
			log.trace("do stopTrack");
			getAudioPlayer().stopTrack();
		});

		waitUntil("track stop", (b) -> BotStatus.VOICE_CHANNEL_IDLE == b.getStatus());
	}

	public synchronized void joinVoiceChannel(VoiceChannel vc) {
		log.trace("queue joinVoiceChannel");
		queue(() -> {
			log.trace("do joinVoiceChannel");
			audioManager.openAudioConnection(vc);
		});

		waitUntil("joined voiceChannel", (b) -> BotStatus.VOICE_CHANNEL_IDLE == b.getStatus());
	}

	public synchronized void leaveVoiceChannel() {
		log.trace("queue leaveVoiceChannel");
		queue(() -> {
			log.trace("do leaveVoiceChannel");
			audioManager.closeAudioConnection();
		});

		waitUntil("left channel", (b) -> BotStatus.NOT_IN_VOICE_CHANNEL == b.getStatus());
	}

	public void waitUntil(String reason, Predicate<BotSession> condition) {
		log.trace("queue waiting until: {}", (Object) reason);

		queue(() -> {
			doWait(reason, condition);
		});
	}

	// ---------- blocking
	private void doWait(String reason, Predicate<BotSession> condition) {
		log.trace("do waiting until: {}", reason);

		final long sleepTime = 100;
		long accumulatedWaitTime = 0;

		boolean sucess = condition.test(this);
		while (!sucess) {
			try {
				Thread.sleep(sleepTime);
				accumulatedWaitTime += sleepTime;

				if (accumulatedWaitTime >= 15000) {
					log.error("wait time timeout");
					break;
				}
			} catch (Exception e) {
				log.warn("sleep interupted", e);
				break;
			}
			sucess = condition.test(this);
		}

		log.trace("end waiting: {}", reason);
	}

	// ---------- non-queue

	public AudioTrack getPlayingTrack() {
		return getAudioPlayer().getPlayingTrack();
	}

	public VoiceChannel getConnectedVoiceChannel() {
		return audioManager.getConnectedChannel();
	}

	// ------------------------------------

	public BotStatus getStatus() {
		return this.status;
	}

	public synchronized void setStatus(BotStatus status) {
		log.trace("set status: {}", status);
		this.status = status;
	}

	@Override
	public boolean canProvide() {
		lastFrame = getAudioPlayer().provide();
		return lastFrame != null;
	}

	@Override
	public ByteBuffer provide20MsAudio() {
		return ByteBuffer.wrap(lastFrame.getData());
	}

	@Override
	public boolean isOpus() {
		return true;
	}

	@Override
	public void onEvent(AudioEvent event) {
		if (event instanceof TrackEndEvent) {
			log.debug("audioEvent: Track End: {}", ((TrackEndEvent) event).track.getUserData());
			setStatus(BotStatus.VOICE_CHANNEL_IDLE);

		} else if (event instanceof TrackStartEvent) {
			log.debug("audioEvent: Track Start: {}", ((TrackStartEvent) event).track.getUserData());
			setStatus(BotStatus.PLAYING_AUDIO);

		} else if (event instanceof TrackExceptionEvent) {
			TrackExceptionEvent ex = (TrackExceptionEvent) event;

			log.debug("audioEvent: TrackExceptionEvent: {}", ex.track.getUserData(), ex.exception);
			getAudioPlayer().stopTrack();
			setStatus(BotStatus.VOICE_CHANNEL_IDLE);

		} else if (event instanceof TrackStuckEvent) {

			log.debug("audioEvent: TrackStuckEvent: {}", ((TrackStuckEvent) event).track.getUserData());
			getAudioPlayer().stopTrack();
			setStatus(BotStatus.VOICE_CHANNEL_IDLE);
		} else {
			log.debug("audioEvent: {}", event.getClass().getSimpleName());
		}
	}

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, guildId + "-" + seqGenerator.incrementAndGet());
	}

	/**
	 * @see {@link ListenerAdapter}
	 */
	@Override
	public void onEvent(GenericEvent event) {

		if (event instanceof GuildVoiceJoinEvent) {
			GuildVoiceJoinEvent e = (GuildVoiceJoinEvent) event;

			if (e.getEntity().getUser().getId().equals(jda.getSelfUser().getId())) {
				setStatus(BotStatus.VOICE_CHANNEL_IDLE);
			}
			return;
		}

		if (event instanceof GuildVoiceLeaveEvent) {
			GuildVoiceLeaveEvent e = (GuildVoiceLeaveEvent) event;

			if (e.getEntity().getUser().getId().equals(jda.getSelfUser().getId())) {
				setStatus(BotStatus.NOT_IN_VOICE_CHANNEL);
			}
			return;
		}

	}

}
