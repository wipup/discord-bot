package wp.discord.bot.core;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import brave.Span;
import brave.Tracer;

@Component
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

	public void clearTraceContext() {
		MDC.remove(X_B3_TRACE_ID);
		MDC.remove(X_B3_SPAN_ID);
	}
}
