package wp.discord.temp.db.entity;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EUser {

	@EqualsAndHashCode.Include
	private String id;

	private List<EAlert> alerts;

}
