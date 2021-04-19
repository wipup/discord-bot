package wp.discord.bot.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageChannel;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.task.helper.HelpTaskHelper;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class HelpActionHandler implements ActionHandler {

	@Autowired
	private HelpTaskHelper helper;

	@Override
	public void handleAction(BotAction action) throws Exception {
		MessageChannel channel = action.getEventMessageChannel();
		if (channel == null) {
			return;
		}

		String param = SafeUtil.get(() -> action.getActionParams().get(0));
		if (StringUtils.isEmpty(param)) {
			replyDefault(action, channel);
			return;
		}

		CmdAction cmd = CmdAction.getMatchingAction(param);
		if (cmd != null) {
			replyCmdActionHelp(cmd, action, channel);
			return;
		}

		CmdEntity entity = CmdEntity.getMatchingEntity(param);
		if (entity != null) {
			replyCmdEntityHelp(cmd, action, channel);
			return;
		}

		replyDefault(action, channel);
	}

	public void replyCmdActionHelp(CmdAction cmd, BotAction action, MessageChannel channel) {
		if (cmd == CmdAction.COMPILE_CRON) {
			Reply rep = helper.getHelpForCompileCron();
			channel.sendMessage(rep.build()).queue();
			return;
		}

		if (cmd == CmdAction.GET) {
			// TODO
			return;
		}
		
		if (cmd == CmdAction.ADD) {
			// TODO
			return;
		}
		
		if (cmd == CmdAction.JOIN_VOICE_CHANNEL) {
			// TODO
			return;
		}
		
		if (cmd == CmdAction.LEAVE_VOICE_CHANNEL) {
			// TODO
			return;
		}
		
		if (cmd == CmdAction.PLAY_AUDIO) {
			// TODO
			return;
		}
		
		if (cmd == CmdAction.SEND_MESSAGE_TO_TEXT_CHANNEL) {
			// TODO
			return;
		}
		
		if (cmd == CmdAction.SEND_MESSAGE_TO_PRIVATE_CHANNEL) {
			// TODO
			return;
		}
		
		replyDefault(action, channel);
	}

	public void replyCmdEntityHelp(CmdAction cmd, BotAction action, MessageChannel channel) {
		replyDefault(action, channel);
	}

	public void replyDefault(BotAction action, MessageChannel channel) {
		log.debug("sending default reply for help");
		Reply rep = helper.getAvailableCommandsTemp();
		channel.sendMessage(rep.build()).queue();
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.HELP;
	}

}
