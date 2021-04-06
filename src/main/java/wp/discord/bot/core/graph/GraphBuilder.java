package wp.discord.bot.core.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import wp.discord.bot.util.ToStringUtils;

@Getter
public class GraphBuilder {

	private static final AtomicInteger NODE_SEQ = new AtomicInteger(0);

	private Node startNode = null;
	private Map<String, Node> allNodes = new HashMap<>(); // key = name, value = node

	public static void main(String[] args) {
		GraphBuilder gb = new GraphBuilder();
		gb.startAt("2");
		gb.from("1").to("2").on("z");
		gb.from("1").to("2").on("gb");
		gb.from("2").to("3").on("a");
		gb.from("1").to("3").on("b");
		gb.from("3").to("3").on("u");
		gb.from("2", "action1").to("1", "action2").on(Type.PATTERN, ".*");

		System.out.println(ToStringUtils.toString(gb));
	}

	public GraphBuilder startAt(Node node) {
		startNode = node;
		allNodes.put(node.getName(), node);
		return this;
	}

	@Deprecated
	public GraphBuilder startAt(String nodeName, String action) {
		return startAt(getExistingOrNewNode(nodeName, action));
	}

	public GraphBuilder startAt(String nodeName) {
		return startAt(nodeName, null);
	}

	public ToBuilder from(Node node) {
		if (startNode == null) {
			startNode = node;
		}
		allNodes.put(node.getName(), node);

		ToBuilder to = new ToBuilder();
		to.from = node;
		return to;
	}

	@Deprecated
	public ToBuilder from(String nodeName, String action) {
		return from(getExistingOrNewNode(nodeName, action));
	}

	public ToBuilder from(String nodeName) {
		return from(nodeName, null);
	}

	public class ToBuilder {

		private Node from = null;

		public OnBuilder to(Node node) {
			allNodes.put(node.getName(), node);

			OnBuilder on = new OnBuilder();
			on.from = from;
			on.to = node;
			return on;
		}

		public OnBuilder to(String nodeName) {
			return to((String) nodeName, (String) null);
		}

		@Deprecated
		public OnBuilder to(String nodeName, String action) {
			return to(getExistingOrNewNode(nodeName, action));
		}
	}

	public class OnBuilder {
		private Node from = null;
		private Node to = null;

		public GraphBuilder on(String value) {
			return on(null, value);
		}

		public GraphBuilder on(Type valueType, String value) {
			valueType = ObjectUtils.defaultIfNull(valueType, Type.EQUALS);
			Objects.requireNonNull(value, "RouteBinding value cannot be null");

			Route r = new Route();
			r.setTo(to);
			r.setToNodeName(to.getName());
			r.setType(valueType);
			r.setValue(value);
			r.setWeight(0);

			from.getRoutes().add(r);
			return GraphBuilder.this;
		}
	}

	protected Node getExistingOrNewNode(String nodeName, String action) {
		if (StringUtils.isBlank(nodeName)) {
			nodeName = String.valueOf("Node-" + NODE_SEQ.incrementAndGet());
		}
		Node node = allNodes.get(nodeName);
		if (node == null) {
			node = new Node(nodeName);
//			node.setAction(action);
		}
		return node;
	}

	public Graph build() {
		return new Graph(startNode, allNodes);
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
