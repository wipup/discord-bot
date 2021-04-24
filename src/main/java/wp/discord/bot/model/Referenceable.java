package wp.discord.bot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import wp.discord.bot.constant.CmdToken;

public interface Referenceable extends Describeable {

	public String entityID();

	public CmdToken entityName();

	@JsonIgnore
	default public Reference getReference() {
		return new Reference(entityName().getCmd(), entityID());
	}

}
