package wp.discord.bot.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum DiscordUserRole {

	OWNER, ADMIN, NORMAL_USER

	;

	public static final List<DiscordUserRole> ALL_ROLES;
	static {
		ALL_ROLES = Collections.unmodifiableList(Arrays.asList(DiscordUserRole.values()));
	}
}
