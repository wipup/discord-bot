package wp.discord.bot.core;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import brave.Span;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * try not to use {@link ListenerAdapter}
 */
@Slf4j
public abstract class AbstractDiscordEventListener<T extends GenericEvent> implements EventListener, InitializingBean {

	@Autowired
	private TracingHandler tracing;

	@Autowired
	private EventErrorHandler errorHandler;

	@Autowired
	private JDA jda;

	@SuppressWarnings("unchecked")
	public void onEvent(GenericEvent event) {
		Span sp = tracing.startNewTrace();
		try {
			if (accept(event)) {
				prepareHandleEvent((T) event);
				handleEvent((T) event);
			}

		} catch (Exception e) {
			handleError(event, e);

		} catch (Throwable t) {
			handleError(event, t);
			throw t;

		} finally {
			tracing.clearTraceContext(sp);
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

	public void handleError(GenericEvent event, Throwable e) {
		errorHandler.handleEventError(event, e);
	}

	public void prepareHandleEvent(T event) throws Exception {
	}

	abstract public void handleEvent(T event) throws Exception;

	abstract public Class<T> eventClass();

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Adding JDA event listener: {}", this);
		jda.addEventListener(this);
	}
}
