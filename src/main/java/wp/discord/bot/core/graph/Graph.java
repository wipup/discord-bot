package wp.discord.bot.core.graph;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wp.discord.bot.util.ToStringUtils;

@Getter
@AllArgsConstructor
public class Graph {

	private Node startNode = null;
	private Map<String, Node> allNodes = new HashMap<>(); // key = name, value = node

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}
