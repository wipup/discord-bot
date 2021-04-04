package wp.discord.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.dv8tion.jda.api.JDA;
import wp.discord.bot.model.DiscordBotContext;

@Configuration
public class DiscordUserConfig {

//	@Autowired
//	private DiscordProperties discordProperties;

	@Bean
	public DiscordBotContext discordBotContext(JDA jda) {
		DiscordBotContext ctx = new DiscordBotContext();

		// validate user, role

		return ctx;
	}
}
