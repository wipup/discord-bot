package wp.discord.bot.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import wp.discord.bot.util.ToStringUtils;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Reference {

	@EqualsAndHashCode.Include
	private String entity;
	@EqualsAndHashCode.Include
	private String id;

	public static Reference construct(String code) {
		String[] frag = code.split("_");
		return new Reference(frag[0], frag[1]);
	}

	public String getCode() {
		return entity + "_" + id;
	}

	@Override
	public String toString() {
		return ToStringUtils.toString(this);
	}
}