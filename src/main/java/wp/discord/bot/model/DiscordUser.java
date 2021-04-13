package wp.discord.bot.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.entities.User;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DiscordUser {

	@EqualsAndHashCode.Include
	private String id;

	private String alias;
	private DiscordUserRole role;
	private User user;

}
