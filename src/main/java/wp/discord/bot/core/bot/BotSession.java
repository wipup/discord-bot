package wp.discord.bot.core.bot;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.slf4j.MDC;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.PlayerPauseEvent;
import com.sedmelluq.discord.lavaplayer.player.event.PlayerResumeEvent;
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
import wp.discord.bot.util.WaitUtil;

@Getter
@Setter
@ToString
@Slf4j
public class BotSession implements AudioSendHandler, AudioEventListener, ThreadFactory, EventListener {

	// internal-core
	private static final AtomicInteger SEQ_GENERATOR = new AtomicInteger(0);

	// bean or whatever
	@ToString.Exclude
	private final ExecutorService executorService;
	@ToString.Exclude
	private final JDA jda;
	@ToString.Exclude
	private AudioManager audioManager;

	// attribute
	private String guildId;
	private Guild guild;
	private BotStatus status;

	// audio
	@ToString.Exclude
	private AudioFrame lastFrame;
	@ToString.Exclude
	private AudioPlayer audioPlayer;

	public BotSession(JDA jda) {
		this.jda = jda;
		this.executorService = Executors.newSingleThreadExecutor(this);
		this.jda.addEventListener(this);
	}

	// ---------- queue
	public void queue(Runnable r) {
		synchronized (executorService) {
			try {
				final Map<String, String> mdc = MDC.getCopyOfContextMap();
				executorService.submit(() -> {
					try {
						MDC.setContextMap(mdc);
						r.run();
					} finally {
						MDC.clear();
					}
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void playTrack(AudioTrack track) {
		synchronized (executorService) {
			log.trace("queue playTrack: {}", track.getUserData());

			queue(() -> {
				log.debug("do playTrack: {}", track.getUserData());
				getAudioPlayer().playTrack(track.makeClone());
			});

			waitUntil("track started: " + track.getUserData(), (b) -> BotStatus.PLAYING_AUDIO == b.getStatus());
			waitUntil("track ended: " + track.getUserData(), (b) -> BotStatus.VOICE_CHANNEL_IDLE == b.getStatus());
		}
	}

	public void stopTrack() {
		synchronized (executorService) {
			log.trace("queue stopTrack");

			queue(() -> {
				log.debug("do stopTrack");
				getAudioPlayer().stopTrack();
			});

			waitUntil("track stopped", (b) -> BotStatus.VOICE_CHANNEL_IDLE == b.getStatus());
		}
	}

	public void joinVoiceChannel(VoiceChannel vc) {
		synchronized (executorService) {
			log.trace("queue joinVoiceChannel: {}", vc);
			queue(() -> {
				log.debug("do joinVoiceChannel: {}", vc);
				audioManager.openAudioConnection(vc);
			});

			waitUntil("joined voiceChannel: " + vc, (b) -> BotStatus.VOICE_CHANNEL_IDLE == b.getStatus());
		}
	}

	public void leaveVoiceChannel() {
		synchronized (executorService) {
			log.trace("queue leaveVoiceChannel");
			queue(() -> {
				log.debug("do leaveVoiceChannel");
				audioManager.closeAudioConnection();
			});

			waitUntil("left channel", (b) -> BotStatus.NOT_IN_VOICE_CHANNEL == b.getStatus());
		}
	}

	public void waitUntil(String reason, Predicate<BotSession> condition) {
		log.trace("queue waiting until: {}", (Object) reason);
		queue(() -> {
			doWait(reason, condition);
		});
	}

	// ---------- blocking
	private void doWait(String reason, Predicate<BotSession> condition) {
		WaitUtil.doWait(reason, ()-> condition.test(this));
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

		} else if (event instanceof PlayerPauseEvent) {
			log.debug("audioEvent: Player Pause");
			setStatus(BotStatus.VOICE_CHANNEL_IDLE);

		} else if (event instanceof PlayerResumeEvent) {
			log.debug("audioEvent: Player Resume");
			setStatus(BotStatus.PLAYING_AUDIO);

		} else {
			log.info("Unknown AudioEvent: {}", event.getClass().getSimpleName());
		}
	}

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, guildId + "-" + SEQ_GENERATOR.incrementAndGet());
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
