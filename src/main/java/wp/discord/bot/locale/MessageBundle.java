package wp.discord.bot.locale;

public interface MessageBundle {

	public String getMessage(String key, Object... objs);

	public String getLanguageCode();

	public boolean matchesKey(String key, String messagePattern);
}
