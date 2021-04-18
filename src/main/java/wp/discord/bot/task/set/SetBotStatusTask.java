package wp.discord.bot.task.set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class SetBotStatusTask {

	@Autowired
	private JDA jda;

	public void setActivityStatus(BotAction action) throws Exception {
		String activityStr = action.getFirstEntitiesParam(CmdEntity.VALUE);
		if ("non".equalsIgnoreCase(activityStr)) {
			jda.getPresence().setActivity(null);
			return;
		}

		ActivityType activityType = getActivityType(activityStr);
		if (activityType == null) {
			Reply reply = Reply.of().bold("Error!").literal(" Unknown activity: " + activityStr);
			throw new BotException(reply.newline().append(getActivityHelp()));
		}

		String value = action.getEntitiesParam(CmdEntity.VALUE, 1);
		if (StringUtils.isBlank(value)) {
			Reply reply = Reply.of().bold("Error!").literal(" Activity value must not be empty");
			throw new BotException(reply);
		}

		Activity activity = Activity.of(activityType, value);
		if (activity == null) {
			Reply reply = Reply.of().bold("Error!").literal(" Can't set activity now");
			throw new BotException(reply);
		}
		jda.getPresence().setActivity(activity);
	}

	public void setOnlineStatus(BotAction action) throws Exception {
		String stat = StringUtils.defaultString(action.getFirstEntitiesParam(CmdEntity.VALUE));
		if (StringUtils.isEmpty(stat)) {
			Reply reply = Reply.of().bold("Error!").literal(" Bot status must not be empty!");
			throw new BotException(reply.newline().append(getOnlineStatusHelp()));
		}

		OnlineStatus status = OnlineStatus.fromKey(stat.toLowerCase());
		if (status == null) {
			status = SafeUtil.get(() -> OnlineStatus.valueOf(stat.toUpperCase()));
		}
		if (status == null) {
			Reply reply = Reply.of().bold("Error!").literal(" Unknown Status: " + stat);
			throw new BotException(reply.newline().append(getOnlineStatusHelp()));
		}

		jda.getPresence().setStatus(status);
	}

	private ActivityType getActivityType(String value) {
		if ("playing".equalsIgnoreCase(value) || "plays".equalsIgnoreCase(value) || "play".equalsIgnoreCase(value)) {
			return ActivityType.DEFAULT;
		}
		return SafeUtil.get(() -> ActivityType.valueOf(value.toUpperCase()));
	}

	public Reply getActivityHelp() {
		return Reply.of().literal("Usable activity: ") //
				.code("playing").literal(", ") //
				.code(ActivityType.STREAMING.name().toLowerCase()).literal(", ") //
				.code(ActivityType.LISTENING.name().toLowerCase()).literal(", ") //
				.code(ActivityType.WATCHING.name().toLowerCase()).literal(", ") //
				.code(ActivityType.DEFAULT.name().toLowerCase()); //
	}

	public Reply getOnlineStatusHelp() {
		return Reply.of().literal("Usable status: ") //
				.code(OnlineStatus.ONLINE.getKey().toLowerCase()).literal(", ") //
				.code(OnlineStatus.IDLE.getKey().toLowerCase()).literal(", ") //
				.code(OnlineStatus.DO_NOT_DISTURB.getKey().toLowerCase()).literal(", ") //
				.code(OnlineStatus.INVISIBLE.getKey().toLowerCase()).literal(", ") //
				.code(OnlineStatus.OFFLINE.getKey().toLowerCase());
	}
}