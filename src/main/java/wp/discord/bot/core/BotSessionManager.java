package wp.discord.bot.core;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import wp.discord.bot.constant.BotStatus;
import wp.discord.bot.util.SafeUtil;

@Component
public class BotSessionManager implements InitializingBean {

	@Autowired
	private JDA jda;

	@Autowired
	private AudioPlayerManager playerManager;

	private Map<String, BotSession> guildSessions;

	public BotSession getBotSession(Guild guild) {
		if (guild == null) {
			return null;
		}
		return guildSessions.get(guild.getId());
	}

	public BotSession getBotSession(GenericEvent event) {
		return getBotSession(SafeUtil.get(() -> (Guild) event.getClass().getMethod("getGuild").invoke(event)));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		guildSessions = new HashedMap<>();
		updateGuildSessions();
	}

	public synchronized void updateGuildSessions() {
		Map<String, BotSession> map = new HashedMap<>();
		for (Guild guild : jda.getGuilds()) {
			BotSession bs = newBotSession(guild);
			map.put(guild.getId(), bs);
		}
		map = Collections.unmodifiableMap(guildSessions);
	}

	public BotSession newBotSession(Guild guild) {
		BotSession bs = new BotSession();
		bs.setGuild(guild);
		bs.setGuildId(guild.getId());
		bs.setAudioPlayer(playerManager.createPlayer());
		bs.setAudioManager(guild.getAudioManager());
		bs.getAudioManager().setSendingHandler(bs);
		
		boolean connected = bs.getAudioManager().isConnected();
		bs.setStatus(connected ? BotStatus.VOICE_CHANNEL_IDLE : BotStatus.NOT_IN_VOICE_CHANNEL);
		
		
		return bs;
	}
}
