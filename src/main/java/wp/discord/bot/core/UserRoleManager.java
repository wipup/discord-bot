package wp.discord.bot.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import wp.discord.bot.config.properties.DiscordProperties;
import wp.discord.bot.config.properties.DiscordUserProperties;
import wp.discord.bot.model.DiscordUser;
import wp.discord.bot.model.DiscordUserRole;

@Slf4j
@Component
public class UserRoleManager implements InitializingBean {

	@Autowired
	private DiscordProperties discordProperties;

	@Autowired
	private JDA jda;

	private Map<DiscordUserRole, Set<DiscordUser>> roleUserMap;
	private Map<String, DiscordUser> userIdMap;

	public DiscordUserRole getRoleOf(DiscordUser user) {
		return user.getRole();
	}

	public DiscordUserRole getRoleOf(User user) {
		return getRoleOf(user.getId());
	}

	public DiscordUserRole getRoleOf(String id) {
		return userIdMap.get(id).getRole();
	}

	// -----------------------------------------------------------------------------------

	@Override
	public void afterPropertiesSet() throws Exception {
		roleUserMap = new HashMap<>();
		userIdMap = new HashMap<>();

		for (DiscordUserProperties userProp : discordProperties.getUsers()) {
			DiscordUser user = convertUser(userProp);
			registerUser(user);
		}
	}

	private DiscordUser convertUser(DiscordUserProperties userProperty) throws Exception {
		String id = userProperty.getSnowflakeId();

		DiscordUser user = new DiscordUser();
		user.setId(id);
		user.setAlias(ObjectUtils.defaultIfNull(userProperty.getAlias(), id));
		user.setRole(ObjectUtils.defaultIfNull(userProperty.getRole(), DiscordUserRole.NORMAL_USER));

		User actualUser = null;
		try {
			actualUser = jda.retrieveUserById(id).submit().get();
			if (actualUser == null) {
				throw new IllegalArgumentException("Not Found user Snowflake-Id: " + userProperty);
			}

			user.setUser(actualUser);
			return user;
		} finally {
			log.debug("user-id={} : {}", id, actualUser);
		}
	}

	private void registerUser(DiscordUser u) {
		userIdMap.put(u.getId(), u);

		Set<DiscordUser> users = roleUserMap.get(u.getRole());
		if (users == null) {
			users = new HashSet<>();
			roleUserMap.put(u.getRole(), users);
		}
		users.add(u);
	}

}
