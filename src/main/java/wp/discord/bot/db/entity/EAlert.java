package wp.discord.bot.db.entity;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.entities.ChannelType;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EAlert {

	@EqualsAndHashCode.Include
	private String id;
	private EUser owner;
	private String cron;

	private List<ChannelType> alertChannels;

}
