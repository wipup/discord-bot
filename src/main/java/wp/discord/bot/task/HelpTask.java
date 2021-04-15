package wp.discord.bot.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.action.ActionHandler;
import wp.discord.bot.core.bot.UserRoleManager;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.model.DiscordUser;
import wp.discord.bot.model.DiscordUserRole;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class HelpTask implements ActionHandler {

	@Autowired
	private UserRoleManager userManager;

	@Autowired
	private JDA jda;

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

	/**
	 * copy from {@link CronExpression#parse(String)} 
	 */
	public void replyCmdActionHelp(CmdAction cmd, BotAction action, MessageChannel channel) {
		if (cmd == CmdAction.COMPILE_CRON) {
			// copy from CronExpression
			final String help = "" //
					+ " &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; second (0-59)\r\n" //
					+ " &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; minute (0 - 59)\r\n" //
					+ " &#9474; &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; hour (0 - 23)\r\n" //
					+ " &#9474; &#9474; &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; day of the month (1 - 31)\r\n" //
					+ " &#9474; &#9474; &#9474; &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; month (1 - 12) (or JAN-DEC)\r\n" //
					+ " &#9474; &#9474; &#9474; &#9474; &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; day of the week (0 - 7)\r\n" //
					+ " &#9474; &#9474; &#9474; &#9474; &#9474; &#9474;          (0 or 7 is Sunday, or MON-SUN)\r\n" //
					+ " &#9474; &#9474; &#9474; &#9474; &#9474; &#9474;\r\n" //
					+ " &#42; &#42; &#42; &#42; &#42; &#42;"; //

			Reply rep = Reply.of() //
					.literal("The cron expression has six single space-separated time and date fields.").newline() //
					.codeBlock(StringEscapeUtils.unescapeHtml4(help)) //
					.bold("1. ").literal("A field may be an asterisk (*), which always stands for\"first-last\". ") //
					.literal("For the \"day of the month\" or \"day of the week\" fields, aquestion mark (?) may be used instead of an asterisk.").newline() //
					//
					.bold("2. ").literal("Ranges of numbers are expressed by two numbers separated with a hyphen(-). The specified range is inclusive.").newline() //
					//
					.bold("3. ").literal("Following a range (or *) with /n specifies the interval of the number's value through the range.").newline();

			channel.sendMessage(rep.build()).queue();
			return;
		}

		replyDefault(action, channel);
	}

	public void replyCmdEntityHelp(CmdAction cmd, BotAction action, MessageChannel channel) {
		replyDefault(action, channel);
	}

	public void replyDefault(BotAction action, MessageChannel channel) {
		log.debug("sending default reply for help");
		
		Reply rep = Reply.of() //
				.startSpoiler() //
				.mention(jda.getSelfUser()).literal(" created by ") //
				.mention(userManager.getUsersOf(DiscordUserRole.OWNER).stream().map(DiscordUser::getUser).findFirst().orElse(null))//
				.endSpoiler();
		channel.sendMessage(rep.build()).queue();
	}

	@Override
	public CmdAction getAcceptedAction() {
		return CmdAction.HELP;
	}

}
