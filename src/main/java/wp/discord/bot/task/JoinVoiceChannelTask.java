package wp.discord.bot.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.ActionHandler;
import wp.discord.bot.core.BotSession;
import wp.discord.bot.core.BotSessionManager;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.bot.BotAction;
import wp.discord.bot.task.helper.VoiceChannelHelper;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.Reply;

@Component
@Slf4j
public class JoinVoiceChannelTask implements ActionHandler {

	@Autowired
	private BotSessionManager sessionManager;

	@Autowired
	private VoiceChannelHelper helper;

	@Autowired
	private JDA jda;

	@Override
	public void handleAction(BotAction action) throws Exception {
		String channelId = action.getEntities().get(CmdEntity.CHANNEL);
		String userId = action.getEntities().get(CmdEntity.USER);

		userId = DiscordFormat.extractId(userId);
		channelId = DiscordFormat.extractId(channelId);

		if (StringUtils.isEmpty(userId)) {
			userId = action.getAuthorId();
			action.getEntities().put(CmdEntity.USER, userId);
			log.debug("set default user-id to author: {}", userId);
		}
		
		if (StringUtils.isNotEmpty(channelId)) {
			joinChannelByChannelId(action);

		} else if (StringUtils.isNotEmpty(userId)) {
			joinChannelByUserId(action);

		} else {
			Reply reply = Reply.of().literal("Invalid voice-channel ").mentionChannel(channelId).newline() //
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new BotException(reply);
		}
	}

	protected void joinChannelByUserId(BotAction action) throws Exception {
		String userId = action.getEntities().get(CmdEntity.USER);
		userId = DiscordFormat.extractId(userId);

		User user = jda.retrieveUserById(userId).complete();
		if (user == null) {
			Reply reply = Reply.of().literal("Not Found ").mention(user).newline() //
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new BotException(reply);
		}

		VoiceChannel vc = helper.findVoiceChannelOfUser(user);
		if (vc == null) {
			Reply reply = Reply.of().literal("Not Found VoiceChannel of ").mention(user).newline() //
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new BotException(reply);
		}

		joinVoiceChannel(action, vc);
	}

	protected void joinChannelByChannelId(BotAction action) throws Exception {
		String channelId = action.getEntities().get(CmdEntity.CHANNEL);
		channelId = DiscordFormat.extractId(channelId);

		VoiceChannel vc = jda.getVoiceChannelById(channelId);
		if (vc == null) {
			Reply reply = Reply.of().literal("Invalid voice-channel ").mentionChannel(channelId).newline() //
					.mentionUser(action.getAuthorId()).literal(" please try again");
			throw new BotException(reply);
		}

		joinVoiceChannel(action, vc);
	}

	protected void joinVoiceChannel(BotAction action, VoiceChannel vc) throws Exception {
		BotSession bs = sessionManager.getBotSession(vc.getGuild());
		if (bs == null) {
			Reply reply = Reply.of().literal("Not member of ").code(vc.getGuild().getName()).newline() //
					.mentionUser(action.getAuthorId()).literal(" please change");
			throw new BotException(reply);
		}

		bs.joinVoiceChannel(vc);
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.JOIN_VOICE_CHANNEL;
	}

}
