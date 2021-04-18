package wp.discord.bot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Referenceable {

	public String entityID();

	public String entityName();

	@JsonIgnore
	default public Reference getReference() {
		return new Reference(entityName(), entityID());
	}

}
