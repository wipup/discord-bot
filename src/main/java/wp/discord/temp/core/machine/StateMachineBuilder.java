package wp.discord.temp.core.machine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import wp.discord.bot.util.ToStringUtils;

@Getter
public class StateMachineBuilder {

	private static final AtomicInteger NODE_SEQ = new AtomicInteger(0);

	private State startNode = null;
	private Map<String, State> allNodes = new HashMap<>(); // key = name, value = node

	public StateMachineBuilder startAt(State node) {
		startNode = node;
		allNodes.put(node.getName(), node);
		return this;
	}

	public StateMachineBuilder startAt(String nodeName) {
		return startAt(getExistingOrNewNode(nodeName));
	}

	public FromBuilder from(State node) {
		if (startNode == null) {
			startNode = node;
		}
		allNodes.put(node.getName(), node);

		FromBuilder to = new FromBuilder();
		to.from = node;
		return to;
	}

	public FromBuilder from(String nodeName) {
		return from(getExistingOrNewNode(nodeName));
	}

	public class FromBuilder {

		private State from = null;

		public ToBuilder to(State node) {
			allNodes.put(node.getName(), node);

			ToBuilder on = new ToBuilder();
			on.from = from;
			on.to = node;
			return on;
		}

		public ToBuilder to(String nodeName) {
			return to(getExistingOrNewNode(nodeName));
		}

	}

	public class ToBuilder {
		private State from = null;
		private State to = null;

		public OnBuilder on(String value) {
			return on(null, value);
		}

		public OnBuilder on(Transition transition) {
			transition.setTo(to);
			transition.setToNodeName(to.getName());

			from.getRules().add(transition);

			OnBuilder notify = new OnBuilder();
			notify.transition = transition;

			return notify;
		}

		public OnBuilder on(Type valueType, String value) {
			valueType = ObjectUtils.defaultIfNull(valueType, Type.getDefaultType());
			Objects.requireNonNull(value, "value cannot be null");

			Transition r = new Transition();
			r.setType(valueType);
			r.setValue(value);

			return on(r);
		}

	}

	public class OnBuilder {
		private Transition transition = null;

		public StateMachineBuilder notify(Collection<StateChangeListener> listeners) {
			Objects.requireNonNull(listeners);
			transition.setListeners(listeners.stream().collect(Collectors.toList()));
			return StateMachineBuilder.this;
		}

		public StateMachineBuilder notify(StateChangeListener listener, StateChangeListener... listeners) {
			Objects.requireNonNull(listener);

			List<StateChangeListener> list = new ArrayList<>();
			list.add(listener);
			CollectionUtils.addAll(list, listeners);

			return notify(list);
		}

		public Transition getTransition() {
			return transition;
		}
	}

	protected State getExistingOrNewNode(String nodeName) {
		if (StringUtils.isBlank(nodeName)) {
			nodeName = String.valueOf("Node-" + NODE_SEQ.incrementAndGet());
		}
		State node = allNodes.get(nodeName);
		if (node == null) {
			node = new State(nodeName);
		}
		return node;
	}

	public StateMachine build() {
		return new StateMachine(startNode, allNodes);
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
