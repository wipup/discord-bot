package wp.discord.bot.core;

import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import brave.Span;
import brave.Tracer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

@Component
@Slf4j
public class TracingHandler {

	public static final String X_B3_TRACE_ID = "x-b3-traceId";
	public static final String X_B3_SPAN_ID = "x-b3-spanId";

	@Autowired
	private Tracer tracer;

	public Span startNewTrace() {
		Span span = tracer.newTrace().start();

		String traceId = span.context().traceIdString();
		String spanId = span.context().spanIdString();

		MDC.put(X_B3_TRACE_ID, traceId);
		MDC.put(X_B3_SPAN_ID, spanId);

		return span;
	}

	public void clearTraceContext(Span span) {
		if (span != null) {
			span.finish();
		}
		clearTraceContext();
	}

	public String getTraceId() {
		return MDC.get(X_B3_TRACE_ID);
	}
	
	public String getSpanId() {
		return MDC.get(X_B3_SPAN_ID);
	}

	public void clearTraceContext() {
		MDC.remove(X_B3_TRACE_ID);
		MDC.remove(X_B3_SPAN_ID);
	}

	public void queue(MessageAction messageAction) { // short version
		messageAction.queue(onSendMessageSuccess(), onSendMessageFail());
	}

	public <T> Consumer<T> trace(Consumer<T> consumer) { // short version
		return addTracingContext(consumer);
	}

	// TODO fix bug : trace ID not match
	public <T> Consumer<T> addTracingContext(Consumer<T> consumer) {
		Map<String, String> oldCtx = MDC.getCopyOfContextMap();
		return (e) -> {
			Span span = null;
			Map<String, String> currentCtx = MDC.getCopyOfContextMap();
			log.debug("MDC CTX old: {}, current: {}", oldCtx, currentCtx);
			try {
				if (MapUtils.isNotEmpty(oldCtx)) {
					MDC.setContextMap(oldCtx);
				} else {
					span = startNewTrace();
				}
				consumer.accept(e);
			} finally {
				MDC.clear();
				if (MapUtils.isNotEmpty(currentCtx)) {
					MDC.setContextMap(currentCtx);
				}
				if (span != null) {
					clearTraceContext(span);
				}
			}
		};
	}

	public Consumer<Message> onSendMessageSuccess() {
		return addTracingContext((message) -> {
			log.debug("Reply with message-id: {} in channel: {}", message.getId(), message.getChannel());
		});
	}

	public Consumer<Throwable> onSendMessageFail() {
		return addTracingContext((ex) -> {
			log.error("Reply message fail with error: {}", ex);
		});
	}
}
