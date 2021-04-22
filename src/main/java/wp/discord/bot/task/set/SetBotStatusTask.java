package wp.discord.bot.task.set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import wp.discord.bot.constant.BotReferenceConstant;
import wp.discord.bot.constant.CmdToken;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class SetBotStatusTask {

	@Autowired
	private JDA jda;

	public void setActivityStatus(BotAction action) throws Exception {
		String activityStr = action.getFirstEntitiesParam(CmdToken.VALUE);
		if (BotReferenceConstant.NONE.equalsIgnoreCase(activityStr)) {
			jda.getPresence().setActivity(null);
			return;
		}

		ActivityType activityType = getActivityType(activityStr);

		String value = action.getEntitiesParam(CmdToken.VALUE, 1);
		if (StringUtils.isBlank(value)) {
			Reply reply = Reply.of().bold("Error!").literal(" Activity value must not be empty");
			throw new ActionFailException(reply);
		}

		Activity activity = Activity.of(activityType, value);
		if (activity == null) {
			Reply reply = Reply.of().bold("Error!").literal(" Can't set activity now");
			throw new ActionFailException(reply);
		}
		jda.getPresence().setActivity(activity);
	}

	public void setOnlineStatus(BotAction action) throws Exception {
		String stat = StringUtils.defaultString(action.getFirstEntitiesParam(CmdToken.VALUE));
		if (StringUtils.isEmpty(stat)) {
			Reply reply = Reply.of().bold("Error!").literal(" Bot status must not be empty!");
			throw new ActionFailException(reply.newline().append(getOnlineStatusHelp()));
		}

		OnlineStatus status = OnlineStatus.fromKey(stat.toLowerCase());
		if (status == null) {
			status = SafeUtil.get(() -> OnlineStatus.valueOf(stat.toUpperCase()));
		}
		if (status == null) {
			Reply reply = Reply.of().bold("Error!").literal(" Unknown Status: " + stat);
			throw new ActionFailException(reply.newline().append(getOnlineStatusHelp()));
		}

		jda.getPresence().setStatus(status);
	}

	private ActivityType getActivityType(String value) throws Exception {
		if (StringUtils.equalsAnyIgnoreCase(value, BotReferenceConstant.ACTIVITY_PLAYING)) {
			return ActivityType.DEFAULT;
		}
		ActivityType type = SafeUtil.get(() -> ActivityType.valueOf(value.toUpperCase()));
		if (type == null) {
			Reply reply = Reply.of().bold("Error!").literal(" Unknown activity: " + value);
			throw new ActionFailException(reply.newline().append(getActivityHelp()));
		}

		return type;
	}

	public Reply getActivityHelp() {
		return Reply.of().literal("Usage: ").code("bot set activity value <activity> <description>").newline() //
				.literal("Usable activity: ") //
				.code("playing").literal(", ") //
				.code(ActivityType.STREAMING.name().toLowerCase()).literal(", ") //
				.code(ActivityType.LISTENING.name().toLowerCase()).literal(", ") //
				.code(ActivityType.WATCHING.name().toLowerCase()).literal(", ") //
				.code(ActivityType.DEFAULT.name().toLowerCase()); //
	}

	public Reply getOnlineStatusHelp() {
		return Reply.of().literal("Usage: ").code("bot set status value <status>").newline() //
				.literal("Usable status: ") //
				.code(OnlineStatus.ONLINE.getKey().toLowerCase()).literal(", ") //
				.code(OnlineStatus.IDLE.getKey().toLowerCase()).literal(", ") //
				.code(OnlineStatus.DO_NOT_DISTURB.getKey().toLowerCase()).literal(", ") //
				.code(OnlineStatus.INVISIBLE.getKey().toLowerCase()).literal(", ") //
				.code(OnlineStatus.OFFLINE.getKey().toLowerCase());
	}
}
