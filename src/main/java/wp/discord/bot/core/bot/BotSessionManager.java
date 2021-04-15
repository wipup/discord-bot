package wp.discord.bot.core.bot;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import wp.discord.bot.constant.BotStatus;
import wp.discord.bot.util.EventUtil;

@Slf4j
@Component
public class BotSessionManager implements InitializingBean {

	@Autowired
	private JDA jda;

	@Autowired
	private AudioPlayerManager playerManager;

	private Map<String, BotSession> guildSessions;

	public Collection<BotSession> getAllSessions() {
		return guildSessions.values();
	}

	public BotSession getBotSession(Guild guild) {
		if (guild == null) {
			return null;
		}
		return guildSessions.get(guild.getId());
	}

	public BotSession getBotSession(GenericEvent event) {
		Guild guild = EventUtil.getGuild(event);
		if (guild == null) {
			User user = EventUtil.getAuthor(event);
			if (user != null) {
				guild = jda.getMutualGuilds(user).stream().findFirst().orElse(null);
			}
		}

		if (guild == null) {
			BotSession defaultSession = guildSessions.values().stream().findFirst().get();
			log.debug("return defaut session: {}", defaultSession.getGuild());
			return defaultSession;
		}
		return getBotSession(guild);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		guildSessions = new HashedMap<>();
		updateGuildSessions();
		log.debug("Guilds: {}", guildSessions);
	}

	public synchronized void updateGuildSessions() {
		Map<String, BotSession> map = new HashedMap<>();
		for (Guild guild : jda.getGuilds()) {
			BotSession bs = newBotSession(guild);
			map.put(guild.getId(), bs);
		}
		guildSessions = Collections.unmodifiableMap(map);
	}

	public BotSession newBotSession(Guild guild) {
		BotSession bs = new BotSession(jda);
		bs.setGuild(guild);
		bs.setGuildId(guild.getId());
		bs.setAudioPlayer(playerManager.createPlayer());
		bs.getAudioPlayer().addListener(bs);
		bs.setAudioManager(guild.getAudioManager());
		bs.getAudioManager().setSendingHandler(bs);

		boolean connected = bs.getAudioManager().isConnected();
		bs.setStatus(connected ? BotStatus.VOICE_CHANNEL_IDLE : BotStatus.NOT_IN_VOICE_CHANNEL);

		return bs;
	}
}
