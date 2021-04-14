package wp.discord.temp.core;

import wp.discord.temp.model.DiscordBotContext;

@Deprecated
public interface ThreadContextAware {

	public static final ThreadLocal<DiscordBotContext> THREAD_CONTEXT = new ThreadLocal<>();

	default public DiscordBotContext getCurrentContext() {
		DiscordBotContext ctx = THREAD_CONTEXT.get();
		if (ctx == null) {
			ctx = new DiscordBotContext();
			setCurrentContext(ctx);
		}
		return ctx;
	}

	default public void setCurrentContext(DiscordBotContext ctx) {
		THREAD_CONTEXT.set(ctx);
	}

	default public void clearCurrentContext() {
		THREAD_CONTEXT.remove();
	}

}