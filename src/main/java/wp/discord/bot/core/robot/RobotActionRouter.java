package wp.discord.bot.core.robot;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.aop.Action;
import wp.discord.bot.util.ToStringUtils;

@Slf4j
@Component
public class RobotActionRouter {

	private boolean init = false;
	private Map<String, List<ExecutionRoute>> executionRoute;

	public void executeAction(String action, Object... args) {
		List<ExecutionRoute> routes = executionRoute.get(action);
		if (CollectionUtils.isEmpty(routes)) {
			log.error("No route register for action={}", action);
			return;
		}

		for (ExecutionRoute r : routes) {
			try {
				executeRoute(r, args);
			} catch (Exception e) {
				log.error("error executing route: {}", r, e);
			}
		}
	}

	private void executeRoute(ExecutionRoute route, Object... args) throws Exception {
		route.method.invoke(route.bean, args);
	}

	@Autowired
	public void configActionAop(Collection<RobotActionTarget> executors) {
		if (init) {
			log.warn("RobotActionRouter has already been initialized");
			return;
		}
		
		Map<String, List<ExecutionRoute>> routeMap = new HashMap<>();
		for (RobotActionTarget exe : executors) {

			Method[] methods = exe.getClass().getDeclaredMethods();
			for (Method m : methods) {

				Class<?> clazz = m.getDeclaringClass();
				Action action = AnnotationUtils.findAnnotation(m, Action.class);
				if (action != null) {
					log.debug("RegisterRoute: {}.{}() = @{}", clazz.getSimpleName(), m.getName(), action.value());

					ExecutionRoute route = newRoute(exe, m);
					registerRoute(routeMap, action, route);
				}
			}
		}

		executionRoute = Collections.unmodifiableMap(routeMap);
		init = true;
	}

	private void registerRoute(Map<String, List<ExecutionRoute>> routeMap, Action action, ExecutionRoute route) {
		List<ExecutionRoute> routes = routeMap.get(action.value());
		if (routes == null) {
			routes = new ArrayList<>();
			routeMap.put(action.value(), routes);
		}
		routes.add(route);
	}

	private ExecutionRoute newRoute(RobotActionTarget bean, Method method) {
		ExecutionRoute route = new ExecutionRoute();
		route.bean = bean;
		route.method = method;
		return route;
	}

	private static class ExecutionRoute {

		RobotActionTarget bean;
		Method method;

		@Override
		public String toString() {
			return ToStringUtils.toString(this);
		}
	}
}
