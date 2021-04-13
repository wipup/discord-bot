package wp.discord.bot.config;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.config.properties.RobotStateProperties;
import wp.discord.bot.config.properties.RobotStateTransitionProperties;
import wp.discord.bot.config.properties.RobotTransitEventProperties;
import wp.discord.bot.core.ThreadContextAware;
import wp.discord.bot.core.action.ActionExecutionInfo;
import wp.discord.bot.core.action.ActionRouter;
import wp.discord.bot.core.machine.State;
import wp.discord.bot.core.machine.StateChangeListener;
import wp.discord.bot.core.machine.StateDriver;
import wp.discord.bot.core.machine.StateMachine;
import wp.discord.bot.core.machine.StateMachineBuilder;
import wp.discord.bot.core.machine.StateMachineBuilder.FromBuilder;
import wp.discord.bot.core.machine.StateMachineBuilder.OnBuilder;
import wp.discord.bot.core.machine.StateMachineBuilder.ToBuilder;
import wp.discord.bot.core.machine.Transition;

@Configuration
@Slf4j
@EnableConfigurationProperties({ RobotStateProperties.class })
public class RobotConfig {

	@Autowired
	private RobotStateProperties robotProperties;

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	private Collection<ActionRouter> router;

	@Bean
	public StateMachine machine() throws Exception {
		log.trace("Robot Properties: {}", robotProperties);

		StateMachineBuilder builder = new StateMachineBuilder();
		builder.startAt(robotProperties.getStartState());

		// on transition
		for (RobotStateTransitionProperties prop : robotProperties.getTransition()) {
			List<String> fromStates = combineFields(prop.getFromStates(), prop.getFromState());
			List<String> toStates = combineFields(prop.getToStates(), prop.getToState());
			List<String> callActions = combineFields(prop.getCallActions(), prop.getCallAction());
			List<RobotTransitEventProperties> onEvents = prop.getOnEvents();

			for (String from : fromStates) {
				FromBuilder fb = builder.from(from);

				for (String to : toStates) {
					ToBuilder tb = fb.to(to);

					for (RobotTransitEventProperties on : onEvents) {
						OnBuilder ob = tb.on(on.getType(), on.getValue());

						if (CollectionUtils.isEmpty(callActions)) {
							log.trace("from: {}, to: {}, on: {}", from, to, on);
							continue;
						}
						// else

						log.trace("from: {}, to: {}, on: {}, action: {}", from, to, on, callActions);
						List<StateChangeListener> actionListeners = callActions.stream()//
								.filter(StringUtils::isNotEmpty) //
								.map((action) -> (StateChangeListener) new TransitionActionListener(action))//
								.collect(Collectors.toList());
						ob.notify(actionListeners);

					}
				}
			}
		}

		return builder.build();
	}

	public class TransitionActionListener implements StateChangeListener, ThreadContextAware {

		private final String action;

		public TransitionActionListener(String action) {
			this.action = action;
		}

		@Override
		public void onStateChange(StateDriver driver, State from, State to, String value, Transition transition) {
			Collection<ActionRouter> routers = getActionRouters();
			for (ActionRouter router : routers) {
				ActionExecutionInfo info = new ActionExecutionInfo();
				info.setFromState(from);
				info.setToState(to);
				info.setEventValue(value);
				info.setAction(getAction());
				
				router.queueExecuteAction(info);
			}
		}

		public String getAction() {
			return action;
		}
	}

	private synchronized Collection<ActionRouter> getActionRouters() {
		if (router == null) {
			router = applicationContext.getBeansOfType(ActionRouter.class).values();
		}
		return router;
	}

	private List<String> combineFields(List<String> list, String single) {
		List<String> newList = CollectionUtils.emptyIfNull(list).stream().collect(Collectors.toList());
		if (StringUtils.isNotBlank(single)) {
			newList.add(single);
		}
		return newList.stream().distinct().filter(StringUtils::isNotBlank).collect(Collectors.toList());
	}

}
