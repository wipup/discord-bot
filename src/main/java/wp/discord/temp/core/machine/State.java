package wp.discord.temp.core.machine;

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
public class State {

	@EqualsAndHashCode.Include
	private String name;

	private List<Transition> rules = new ArrayList<>();

	public State(String name) {
		super();
		this.name = name;
	}

	public boolean hasMoreTransitionRules() {
		return CollectionUtils.isNotEmpty(rules);
	}

	public Transition findFirstTransition(String value) {
		for (Transition r : rules) {
			if (r.canAccept(value)) {
				return r;
			}
		}
		return null;
	}

	public List<Transition> findAllTransitionRules(String value) {
		return rules.stream().filter((r) -> r.canAccept(value)).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}

}
