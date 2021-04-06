package wp.discord.bot.core.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import wp.discord.bot.util.ToStringUtils;

/**
 * Node or Vertex
 * 
 * @author PC
 *
 */
@Getter
@Setter(value = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Node {

	@EqualsAndHashCode.Include
	private String name;

	private List<Route> routes = new ArrayList<>();

	public Node(String name) {
		super();
		this.name = name;
	}

	public boolean hasMoreRoute() {
		return CollectionUtils.isNotEmpty(routes);
	}

	public Route findFirstRoute(String value) {
		for (Route r : routes) {
			if (r.canAccept(value)) {
				return r;
			}
		}
		return null;
	}

	public List<Route> findAllRoutes(String value) {
		return routes.stream().filter((r) -> r.canAccept(value)).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}

}
