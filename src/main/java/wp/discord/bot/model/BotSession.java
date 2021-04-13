package wp.discord.bot.model;

import lombok.Data;
import net.dv8tion.jda.api.entities.Guild;

@Data
public class BotSession {

	private String guildId;
	private Guild guild;
	private BotStatus status;

}
