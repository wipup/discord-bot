package wp.discord.bot.constant;

import java.util.Arrays;

import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;

public enum Reaction {

	CHECKED("white_check_mark", "\u2705"), //
	NO_CHECKED("negative_squared_cross_mark", "\u274e"), //

	// -----------
	ALERT_ON("bell", "\u1f514"), //
	ALERT_OFF("no_bell ", "\u1f515"), //

	// -----------
	UP("arrow_double_up", "\u23eb"), //
	RIGHT("fast_forward", "\u23e9"), //
	LEFT("rewind", "\u23ea"), //
	DOWN("arrow_double_down", "\u23ec"), //
	
	PLAY("arrow_forward", "▶️"), //
	
	POINT_RIGHT("point_right", "👉🏻"), 
	NEXT_TRACK("next_track", "⏭️"), 
	PREVIOUS_TRACK("previous", "⏮️"), 
	
	// -----------
	OK("ok", "\u1f197"), //
	EDIT("pencil", "\u1f4dd"), //

	// -----------
	TEXT("speech_balloon", "\u1f4ac"), //
	CLOCK("alarm_clock", "\u23f0"), //

	;

	private String name;
	private String code;

	private Reaction(String name, String code) {
		this.name = name.trim();
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public static Reaction getReaction(ReactionEmote emote) {
		String unicode = emote.getAsReactionCode();
		return Arrays.stream(Reaction.values()).filter((r) -> unicode.equals(r.getCode())).findFirst().orElse(null);
	}
}
