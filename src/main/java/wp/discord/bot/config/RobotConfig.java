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
import wp.discord.bot.core.action.ActionSelector;
import wp.discord.bot.core.machine.State;
import wp.discord.bot.core.machine.StateChangeListener;
import wp.discord.bot.core.machine.StateDriver;
import wp.discord.bot.core.machine.StateMachine;
import wp.discord.bot.core.machine.StateMachineBuilder;
import wp.discord.bot.core.machine.StateMachineBuilder.FromBuilder;
import wp.discord.bot.core.machine.StateMachineBuilder.OnBuilder;
import wp.discord.bot.core.machine.StateMachineBuilder.ToBuilder;
import wp.discord.bot.model.CommandContext;
import wp.discord.bot.core.machine.Transition;

@Configuration
@Slf4j
@EnableConfigurationProperties({ RobotStateProperties.class })
public class RobotConfig {

	@Autowired
	private RobotStateProperties robotProperties;

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	private Collection<ActionSelector> router;

	@Bean
	public StateMachine machine() throws Exception {
		log.debug("Robot Properties: {}", robotProperties);
//		Set<String> allStates = new HashSet<>();
//
//		final String finalState = robotProperties.getFinalState();
//		final String finishEvent = robotProperties.getFinishEvent();
//		final String errorEvent = robotProperties.getTerminateEvent();

		StateMachineBuilder builder = new StateMachineBuilder();
		builder.startAt(robotProperties.getStartState());

		// on transition
		for (RobotStateTransitionProperties e : robotProperties.getTransition()) {
			List<String> fromStates = combineFields(e.getFromStates(), e.getFromState());
			List<String> toStates = combineFields(e.getToStates(), e.getToState());
			List<String> onEvents = combineFields(e.getOnEvents(), e.getOnEvent());

			for (String from : fromStates) {
				FromBuilder fb = builder.from(from);

				for (String to : toStates) {
					ToBuilder tb = fb.to(to);

					for (String on : onEvents) {
						OnBuilder ob = tb.on(on);

//						Transition t = ob.getTransition();

						String action = e.getCallAction();
						if (StringUtils.isNotBlank(action)) {
							ob.notify(new TransitionActionListener(action));

							log.debug("from: {}, to: {}, on: {}, method: {}", from, to, on, action);
						} else {
							log.debug("from: {}, to: {}, on: {}", from, to, on);
						}
					}
				}

			}

		}

		return builder.build();
	}

	public class TransitionActionListener implements StateChangeListener {

		private final String action;

		public TransitionActionListener(String action) {
			this.action = action;
		}

		@Override
		public void onStateChange(StateDriver driver, State from, State to, String value, Transition transition) {
			Collection<ActionSelector> routers = getActionRouters();
			for (ActionSelector router : routers) {
				CommandContext cmdContext = router.getCurrentContext().getCommandContext();
				cmdContext.setAction(getAction());
				router.executeAction(getAction(), cmdContext);
			}
		}

		public String getAction() {
			return action;
		}
	}

	private synchronized Collection<ActionSelector> getActionRouters() {
		if (router == null) {
			router = applicationContext.getBeansOfType(ActionSelector.class).values();
		}
		return router;
	}

//	@Bean
//	public UntypedStateMachineBuilder robotBuilder() throws Exception {
//		log.debug("Robot Properties: {}", robotProperties);
//		Set<String> allStates = new HashSet<>();
//
//		final String finalState = robotProperties.getFinalState();
//		final String finishEvent = robotProperties.getFinishEvent();
//		final String errorEvent = robotProperties.getTerminateEvent();
//
//		UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(RobotCore.class);
//
//		builder.defineTerminateEvent(errorEvent);
//		builder.defineFinalState(finalState);
//		builder.defineFinishEvent(finishEvent);
//		builder.defineStartEvent(robotProperties.getStartEvent());
//
//		// on transition
//		for (RobotStateTransitionProperties e : robotProperties.getTransition()) {
//
//			List<String> fromStates = combineFields(e.getFromStates(), e.getFromState());
//			List<String> toStates = combineFields(e.getToStates(), e.getToState());
//			List<String> onEvents = combineFields(e.getOnEvents(), e.getOnEvent());
//
//			for (String from : fromStates) {
//				From<?, Object, Object, Object> f = builder.transition().from(from);
//				
//				for (String to : toStates) {
//					To<?, Object, Object, Object> t = f.to(to);
//
//					for (String on : onEvents) {
//						On<?, Object, Object, Object> o = t.on(on);
//
//						String method = e.getCallMethod();
//						if (StringUtils.isNotBlank(method)) {
//							o.callMethod(method);
//
//							log.debug("from: {}, to: {}, on: {}, method: {}", from, to, on, method);
//						} else {
//							log.debug("from: {}, to: {}, on: {}", from, to, on);
//						}
//					}
//				}
//
//			}
//
//			allStates.addAll(fromStates);
//			allStates.addAll(toStates);
//		}
//
//		// on entry
//		if (CollectionUtils.isNotEmpty(robotProperties.getOnEntry())) {
//			for (RobotStateChangeProperties e : robotProperties.getOnEntry()) {
//				builder.onEntry(e.getState()).callMethod(e.getCallMethod());
//				allStates.add(e.getState());
//			}
//		}
//
//		// on exit
//		if (CollectionUtils.isNotEmpty(robotProperties.getOnExit())) {
//			for (RobotStateChangeProperties e : robotProperties.getOnExit()) {
//				builder.onExit(e.getState()).callMethod(e.getCallMethod());
//				allStates.add(e.getState());
//			}
//		}
//
//		// wired-up all state to terminate state
//		allStates.remove(robotProperties.getFinalState());
//		
//		for (String state : allStates) {
//			builder.transition().from(state).to(finalState).on(finishEvent);
//			log.debug("from: {}, to: {}, on: {}", state, finalState, finishEvent);
//			
//			builder.transition().from(state).to(finalState).on(errorEvent);
//			log.debug("from: {}, to: {}, on: {}", state, finalState, errorEvent);
//		}
//
//		return builder;
//	}

	private List<String> combineFields(List<String> list, String single) {
		List<String> newList = CollectionUtils.emptyIfNull(list).stream().collect(Collectors.toList());
		if (StringUtils.isNotBlank(single)) {
			newList.add(single);
		}
		return newList.stream().distinct().filter(StringUtils::isNotBlank).collect(Collectors.toList());
	}

}
