package wp.discord.temp.core.action;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.util.ToStringUtils;
import wp.discord.temp.core.ThreadContextAware;
import wp.discord.temp.model.CommandContext;

@Slf4j
@Component
public class ActionRouter implements ThreadContextAware, InitializingBean {

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	private Map<String, List<ActionExecutionRoute>> executionRoutes;

	public void queueExecuteAction(ActionExecutionInfo info) {
		List<ActionExecutionRoute> routes = executionRoutes.get(info.getAction());
		if (CollectionUtils.isEmpty(routes)) {
			log.error("No route register for action={}", info.getAction());
			return;
		}

		info.setRoutes(routes);
		getCurrentContext().getMethodInvokeQueue().add(info);
	}

	public void executeQueuedActions() throws Exception {
		CommandContext cmdContext = getCurrentContext().getCommandContext();
		for (ActionExecutionInfo info : getCurrentContext().getMethodInvokeQueue()) {

			cmdContext.setAction(info.getAction());
			cmdContext.setActionValue(info.getEventValue());

			for (ActionExecutionRoute route : info.getRoutes()) {
				log.debug("execute action: {}, with: {}", info.getAction(), info.getEventValue());
				executeRoute(route, (Object) cmdContext);

				if (cmdContext.isActionError()) {
					log.debug("action ends with error: {}", info.getAction(), info.getEventValue());
					return;
				}
			}
		}
	}

	protected void executeRoute(ActionExecutionRoute route, Object... args) throws Exception {
		route.getMethod().invoke(route.getBean(), args);
	}

	// ------------------------------------------------------------------------

	@Override
	public void afterPropertiesSet() throws Exception {
		Collection<Object> beans = applicationContext.getBeansWithAnnotation(ActionExecutor.class).values();
		registerActionExecutors(beans);
	}

	private void registerActionExecutors(Collection<Object> executors) {
		Map<String, List<ActionExecutionRoute>> routeMap = new HashMap<>();
		for (Object bean : executors) {

			Method[] methods = bean.getClass().getDeclaredMethods();
			for (Method m : methods) {

				Class<?> clazz = m.getDeclaringClass();
				Action action = AnnotationUtils.findAnnotation(m, Action.class);
				if (action != null) {
					log.debug("RegisterRoute: {}.{}() = @{}", clazz.getSimpleName(), m.getName(), action.value());

					ActionExecutionRoute route = newRoute(bean, m);
					registerRoute(routeMap, action, route);
				}
			}
		}

		executionRoutes = Collections.unmodifiableMap(routeMap);
	}

	private void registerRoute(Map<String, List<ActionExecutionRoute>> routeMap, Action action, ActionExecutionRoute route) {
		List<ActionExecutionRoute> routes = routeMap.get(action.value());
		if (routes == null) {
			routes = new ArrayList<>();
			routeMap.put(action.value(), routes);
		}
		routes.add(route);
	}

	private ActionExecutionRoute newRoute(Object bean, Method method) {
		ExecutionRoute route = new ExecutionRoute();
		route.bean = bean;
		route.method = method;
		return route;
	}

	private static class ExecutionRoute implements ActionExecutionRoute {

		private Object bean;
		private Method method;

		@Override
		public String toString() {
			return ToStringUtils.toString(this);
		}

		@Override
		public Object getBean() {
			return bean;
		}

		@Override
		public Method getMethod() {
			return method;
		}
	}

}
