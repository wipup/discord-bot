package wp.discord.bot.core.bot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import wp.discord.bot.config.properties.DiscordProperties;
import wp.discord.bot.config.properties.DiscordUserProperties;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.model.DiscordUser;
import wp.discord.bot.model.DiscordUserRole;
import wp.discord.bot.util.DiscordFormat;
import wp.discord.bot.util.SafeUtil;

@Slf4j
@Component
public class UserManager implements InitializingBean {

	@Autowired
	private DiscordProperties discordProperties;

	@Autowired
	private JDA jda;

	private Map<DiscordUserRole, Set<DiscordUser>> roleUserMap;
	private Map<String, DiscordUser> userIdMap;

	public User getThisBotUser() {
		return jda.getSelfUser();
	}

	public boolean isThisBot(String userId) {
		return jda.getSelfUser().getId().equals(userId);
	}

	public boolean isThisBot(User user) {
		return isThisBot(user.getId());
	}

	public DiscordUser getOwnerUser() {
		return Optional.of(getUsersOf(DiscordUserRole.OWNER)).filter((l) -> !l.isEmpty()).map((l) -> l.get(0)).orElse(null);
	}

	public List<DiscordUser> getUsersOf(DiscordUserRole role) {
		return Optional.ofNullable(roleUserMap.get(role)).orElseGet(() -> new HashSet<>()).stream().collect(Collectors.toList());
	}

	public DiscordUserRole getRoleOf(DiscordUser user) {
		return user.getRole();
	}

	public DiscordUserRole getRoleOf(User user) {
		return getRoleOf(user.getId());
	}

	public DiscordUserRole getRoleOf(String id) {
		DiscordUserRole role = SafeUtil.get(() -> userIdMap.get(id).getRole());
		if (role == null) {
			role = DiscordUserRole.NORMAL_USER;
		}
		return role;
	}

	public User getUserEntity(String userId) {
		return SafeUtil.get(() -> jda.retrieveUserById(userId).complete());
	}

	public User getUserEntity(BotAction action) {
		return getUserEntity(getUserEntityId(action));
	}

	public String getUserEntityId(BotAction action) {
		String userId = StringUtils.trim(action.getFirstEntitiesParam(CmdEntity.USER));
		if ("me".equalsIgnoreCase(userId)) {
			return action.getAuthorId();
		}

		if ("bot".equalsIgnoreCase(userId)) {
			return jda.getSelfUser().getId();
		}

		return DiscordFormat.extractId(userId);
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

		DiscordUser user = getOwnerUser();
		if (user == null) {
			throw new IllegalStateException("There must be at least one OWNER user");
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
			log.debug("user-id={} [{}]: {}", id, user.getRole(), actualUser);
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
