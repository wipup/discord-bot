package wp.discord.bot.task.helper;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import wp.discord.bot.core.bot.UserManager;
import wp.discord.bot.model.DiscordUser;
import wp.discord.bot.model.DiscordUserRole;
import wp.discord.bot.util.Reply;

@Component
public class HelpTaskHelper {

	@Autowired
	private UserManager userManager;

	/**
	 * Copied from {@link CronExpression#parse(String)}
	 * 
	 * @return
	 */
	public Reply getHelpForCompileCron() {
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
				//
				.bold("1. ").literal("A field may be an asterisk (*), which always stands for\"first-last\". ") //
				.literal("For the \"day of the month\" or \"day of the week\" fields, aquestion mark (?) may be used instead of an asterisk.").newline() //
				//
				.bold("2. ").literal("Ranges of numbers are expressed by two numbers separated with a hyphen(-). The specified range is inclusive.").newline() //
				//
				.bold("3. ").literal("Following a range (or *) with /n specifies the interval of the number's value through the range.").newline();

		return rep;
	}

	public Reply getAvailableCommandsTemp() {
		Reply rep = Reply.of() //
				.code("bot set status value <status>").newline() //
				.code("bot set activity value <activity> <name>").newline() //
				.code("bot get log [name <pattern>]").newline() //
				.code("bot delete message id <message-id> [channel <channel-id>] ").newline() //
				.code("bot update audio").newline() //
				.code("bot shutdown").newline() //
				
				.code("bot help cron").newline() //
				.code("bot cron <cron-expr> [count <samplig-count>]").newline() //

				.code("bot add schedule [name <any-name>] [active <true/false>] [count <desired-count/none>] [cron/time <value>] cmd <cmd1 cmd2 cmd3...>").newline() //
				.code("bot add schedule [name <any-name>] [active <true/false>] [count <desired-count/none>] cron <cron-expr> cmd <cmd1 cmd2 cmd3...>").newline() //
				.code("bot add schedule [name <any-name>] [active <true/false>] [count <desired-count/none>] [time <yyyy-MM-ddTHH:mm:ss>] [repeat <ISO-8601>] cmd <cmd1 cmd2 cmd3...>").newline() //
				.code("bot add schedule [name <any-name>] [active <true/false>] [time <now>] cmd <cmd1 cmd2 cmd3...>").newline() //
				.code("bot add schedule [name <any-name>] [active <true/false>] [time <yyyy-MM-ddTHH:mm:ss>] cmd <cmd1 cmd2 cmd3...>").newline() //
				.code("bot get schedule [id <schedule-id>]").newline() //
				.code("bot update schedule [id <schedule-id>] [name <any-name>] [cron <cron>]  [active <true/false>]").newline() //
				.code("bot update schedule [id <schedule-id>] [name <any-name>] [time <yyyy-MM-ddTHH:mm:ss>] [repeat <ISO-8601>] [active <true/false>]").newline() //
				.code("bot update schedule [id <schedule-id>] [name <any-name>] [time same] [repeat same] [active <true/false>]").newline() //
				.code("bot update schedule [id <schedule-id>] [name <any-name>] [time same] [repeat none] [active <true/false>]").newline() //
				.code("bot delete schedule id <schedule-id>").newline() //

				.code("bot get audio [name <name>]").newline() //
				.code("bot play audio <audio-name> [channel <channel-id>] [user <user-id>]").newline() //
				.code("bot play audio '<audio1> , <audio2>, <audio3>'").newline() //
				.code("bot pm [user <user-id>] message <any-message>").newline() //
				.code("bot send [channel <channel-id>] message <any-message>").newline() //
				.code("bot join [channel <channel-id>] [user <user-id>] ").newline() //
				.code("bot leave [channel <channel-id>]  ").newline() //
		;

		return rep;
	}

	public Reply getCreatorInfo() {
		Reply rep = Reply.of() //
				.startSpoiler() //
				.mention(userManager.getThisBotUser()).literal(" created by ") //
				.mention(userManager.getUsersOf(DiscordUserRole.OWNER).stream().map(DiscordUser::getUser).findFirst().orElse(null))//
				.endSpoiler();

		return rep;
	}
}
