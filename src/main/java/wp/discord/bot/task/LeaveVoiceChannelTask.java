package wp.discord.bot.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.VoiceChannel;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.ActionHandler;
import wp.discord.bot.core.BotSession;
import wp.discord.bot.core.BotSessionManager;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.bot.BotAction;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.Reply;

@Component
@Slf4j
public class LeaveVoiceChannelTask implements ActionHandler {

	@Autowired
	private BotSessionManager sessionManager;

	@Autowired
	private JDA jda;

	@Override
	public void handleAction(BotAction action) throws Exception {
		BotSession session = action.getSession();
		if (session != null) {
			session.leaveVoiceChannel();
			return;
		}

		String channelId = action.getEntities().get(CmdEntity.CHANNEL);
		channelId = DiscordFormat.extractId(channelId);

		if (StringUtils.isEmpty(channelId)) {
			log.info("leaving all channel session");
			sessionManager.getAllSessions().forEach(BotSession::leaveVoiceChannel);
			return;
		}

		VoiceChannel vc = jda.getVoiceChannelById(channelId);
		if (vc == null) {
			Reply reply = Reply.of().literal("Invalid voice-channel ").mentionChannel(channelId).newline() //
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new BotException(reply);
		}

		BotSession bs = sessionManager.getBotSession(vc.getGuild());
		if (bs == null) {
			Reply reply = Reply.of().literal("Not member of ").code(vc.getGuild().getName()).newline() //
					.mentionUser(action.getAuthorId()).literal(" please change");
			throw new BotException(reply);
		}

		bs.leaveVoiceChannel();
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.LEAVE_VOICE_CHANNEL;
	}

}