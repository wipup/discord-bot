package wp.discord.bot.task.audio;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.VoiceChannel;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.core.bot.BotSession;
import wp.discord.bot.core.bot.BotSessionManager;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.Reply;

@Component
@Slf4j
public class LeaveVoiceChannelActionHandler implements ActionHandler {

	@Autowired
	private BotSessionManager sessionManager;

	@Autowired
	private JDA jda;

	@Override
	public void handleAction(BotAction action) throws Exception {
		BotSession session = action.getSession();
		if (session != null) {
			session.leaveVoiceChannelNow();
			return;
		}

		String channelId = action.getFirstTokenParam(CmdToken.CHANNEL);
		channelId = DiscordFormat.extractId(channelId);

		if (StringUtils.isEmpty(channelId)) {
			log.info("leaving all channel session");
			sessionManager.getAllSessions().forEach(BotSession::leaveVoiceChannelNow);
			return;
		}

		VoiceChannel vc = jda.getVoiceChannelById(channelId);
		if (vc == null) {
			Reply reply = Reply.of().literal("Invalid voice-channel ").mentionChannel(channelId).newline() //
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new ActionFailException(reply);
		}

		BotSession bs = sessionManager.getBotSession(vc.getGuild());
		if (bs == null) {
			Reply reply = Reply.of().literal("Not member of ").code(vc.getGuild().getName()).newline() //
					.mentionUser(action.getAuthorId()).literal(" please change");
			throw new ActionFailException(reply);
		}

		bs.leaveVoiceChannelNow();
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.LEAVE_VOICE_CHANNEL;
	}

}
