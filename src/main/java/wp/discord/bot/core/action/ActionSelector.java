package wp.discord.bot.core.action;

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
import wp.discord.bot.core.ThreadContextAware;
import wp.discord.bot.model.CommandContext;
import wp.discord.bot.util.ToStringUtils;

@Slf4j
@Component
public class ActionSelector implements ThreadContextAware, InitializingBean {

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	private Map<String, List<ActionExecutionRoute>> executionRoute;

	public void queueExecuteAction(String action) {
		List<ActionExecutionRoute> routes = executionRoute.get(action);
		if (CollectionUtils.isEmpty(routes)) {
			log.error("No route register for action={}", action);
			return;
		}

		getCurrentContext().getMethodInvokeQueue().addAll(routes);
	}

	public void executeQueuedActions() throws Exception {
		CommandContext cmdContext = getCurrentContext().getCommandContext();
		for (ActionExecutionRoute exe : getCurrentContext().getMethodInvokeQueue()) {
			executeRoute(exe, (Object) cmdContext);
		}
	}

	protected void executeRoute(ActionExecutionRoute route, Object... args) throws Exception {
		route.getMethod().invoke(route.getBean(), args);
	}

	// ------------------------------------------------------------------------

	@Override
	public void afterPropertiesSet() throws Exception {
		Collection<Object> beans = applicationContext.getBeansWithAnnotation(ActionExecutorService.class).values();
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

		executionRoute = Collections.unmodifiableMap(routeMap);
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
