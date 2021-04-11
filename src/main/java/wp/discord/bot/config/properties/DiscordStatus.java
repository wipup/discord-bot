package wp.discord.bot.config.properties;

import lombok.Data;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import wp.discord.bot.util.ToStringUtils;

@Data
public class DiscordStatus {

	private String name;
	private ActivityType type;
	private OnlineStatus status;

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}

}
