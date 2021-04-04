package wp.discord.bot.core;

import org.springframework.beans.factory.annotation.Autowired;

import brave.Span;
import brave.Tracer;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public abstract class DiscordEventListener<T extends GenericEvent> implements EventListener, ThreadContextAware {

	@Autowired
	private Tracer tracer;

	@SuppressWarnings("unchecked")
	public void onEvent(GenericEvent event) {
		Span sp = null;
		try {
			sp = tracer.newTrace().start();
			try {
				if (accept(event)) {
					handleEvent((T) event);
				}
			} catch (Exception e) {
				handleError(event, e);
			} finally {
				clearCurrentContext();
			}
		} finally {
			if (sp != null) {
				sp.finish();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public boolean accept(GenericEvent event) {
		if (acceptType(event)) {
			T e = (T) event;
			return acceptCondition(e);
		}
		return false;
	}

	public boolean acceptType(GenericEvent event) {
		Class<T> clazz = eventClass();
		return clazz.isInstance(event);
	}

	public boolean acceptCondition(T event) {
		return true;
	}

	public void handleError(GenericEvent event, Exception e) {
		e.printStackTrace(System.out); // TODO
	}

	public void handleEvent(T event) throws Exception {

	}

	abstract public Class<T> eventClass();

}
