package wp.discord.bot.db.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ScheduledType {

	CRON("Cron"), //
	
	FIXED_RATE("Fixed-Rate"), //
	
	TIME("Time"),

	;

	private String displayName;

}
