package wp.discord.bot.core;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public interface DiscordEventListener<T extends GenericEvent> extends EventListener, ThreadContextAware {

	@SuppressWarnings("unchecked")
	@Override
	default void onEvent(GenericEvent event) {
		try {
			if (accept(event)) {
				handleEvent((T) event);
			}
		} catch (Exception e) {
			handleError(event, e);
		} finally {
			clearAllThreadContext();
		}
	}

	@SuppressWarnings("unchecked")
	default public boolean accept(GenericEvent event) {
		if (acceptType(event)) {
			T e = (T) event;
			return acceptCondition(e);
		}
		return false;
	}

	default public boolean acceptType(GenericEvent event) {
		Class<T> clazz = eventClass();
		return clazz.isInstance(event);
	}

	default public boolean acceptCondition(T event) {
		return true;
	}

	default public void handleError(GenericEvent event, Exception e) {
		e.printStackTrace(); // TODO
	}

	public void handleEvent(T event) throws Exception;

	public Class<T> eventClass();

}
