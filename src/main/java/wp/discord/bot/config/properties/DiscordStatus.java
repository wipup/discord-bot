package wp.discord.bot.config.properties;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import wp.discord.bot.util.ToStringUtils;

public class DiscordStatus {

	private String name;
	private ActivityType type;
	private OnlineStatus status;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ActivityType getType() {
		return type;
	}

	public void setType(ActivityType type) {
		this.type = type;
	}

	public OnlineStatus getStatus() {
		return status;
	}

	public void setStatus(OnlineStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}

}
