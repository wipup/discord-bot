package wp.discord.bot.core.action;

public class ActionConstant {
	public static final String ACTION_GREET_AUTHOR = "do.greet-author";

	public static final String ACTION_LOG_OUT = "do.log-out";
	public static final String ACTION_JOIN_VOICE_CHANNEL = "do.join-voiceChannel";
	public static final String ACTION_LEAVE_VOICE_CHANNEL = "do.leave-voiceChannel";

	public static final String ACTION_GET_AUTHOR_VOICE_CHANNEL = "get.authorVoiceChannel";
	public static final String ACTION_SET_TO_VOICE_CHANNEL = "set.toVoiceChannel";
	
	public static final String ACTION_GET_CURRENT_BOT_VOICE_CHANNEL = "get.currentBotVoiceChannel";
	public static final String ACTION_JOIN_AUTHOR_VOICE_CHANNEL = "do.join-authorVoiceChannel";
	public static final String ACTION_JOIN_USER_VOICE_CHANNEL = "do.join-userVoiceChannel";

	public static final String ACTION_VALIDATE_BOT_IN_AUTHOR_VOICE_CHANNEL = "validate.bot-in-authorVoiceChannel";
	public static final String ACTION_LEAVE_VOICE_CHANNEL_AFTER_AUDIO_END = "set.leave-VoiceChannel-after-AudioEnd";
	public static final String ACTION_PLAY_AUDIO_IN_VOICE_CHANNEL = "do.playAudio-inVoiceChannel";
	public static final String ACTION_STOP_AUDIO_IN_VOICE_CHANNEL = "do.stopAudio-inVoiceChannel";

	public static final String ACTION_SET_TARGET_USER = "set.targetUser";
	public static final String ACTION_ALLOW_ADMINS_AND_OWNER = "allow.admins-or-owner";
	public static final String ACTION_ALLOW_ONLY_OWNER = "allow.only-owner";
	public static final String ACTION_ALLOW_ONLY_USER_ID = "allow.only-user-id";
}
