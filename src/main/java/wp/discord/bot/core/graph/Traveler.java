package wp.discord.bot.core.graph;

import lombok.Getter;
import wp.discord.bot.util.ToStringUtils;

@Getter
public class Traveler implements Acceptable<String> {

	private Graph map;
	private Node currentNode;
	private Node previousNode;

	public Traveler(Graph graph) {
		reset(graph);
	}

	public void accept(String value) {
		Route r = findRoute(value);
		if (r != null) {
			previousNode = currentNode;
			currentNode = r.getTo();
		}
	}

	@Override
	public boolean canAccept(String value) {
		Route r = findRoute(value);
		if (r == null) {
			return false;
		}
		return true;
	}

	private Route findRoute(String value) {
		return currentNode.findFirstRoute(value); // TODO maybe find all routes
	}
	
	public void reset(Graph graph) {
		if (graph != null) {
			this.map = graph;
		}
		reset();
	}

	public void reset() {
		currentNode = map.getStartNode();
		previousNode = null;
	}

	public boolean isEnd() {
		return !currentNode.hasMoreRoute();
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}

}
