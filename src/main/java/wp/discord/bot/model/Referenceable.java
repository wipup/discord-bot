package wp.discord.bot.model;

public interface Referenceable {

	public String entityID();

	public String entityName();

	default public Reference getReference() {
		return new Reference(entityName(), entityID());
	}

}
