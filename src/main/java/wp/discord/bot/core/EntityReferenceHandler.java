package wp.discord.bot.core;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.entities.Message;
import wp.discord.bot.model.Reference;
import wp.discord.bot.model.Referenceable;
import wp.discord.bot.util.SafeUtil;

@Component
public class EntityReferenceHandler {

	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	public String generateEncodedReferenceCode(Referenceable entity) {
		String raw = entity.getReference().getCode();

		String encoded = Base64.getEncoder().encodeToString(raw.getBytes(DEFAULT_CHARSET));
		if (encoded.endsWith("==")) {
			encoded = encoded.substring(0, encoded.length() - 2);
			
		} else if (encoded.endsWith("=")) {
			encoded = encoded.substring(0, encoded.length() - 1);

		}
		return " [" + encoded + "]";
	}

	public Reference decodeReferenceCode(String encoded) {
		String decoded = new String(Base64.getDecoder().decode(encoded), DEFAULT_CHARSET);
		return Reference.construct(decoded);
	}

	public Reference getReference(Message message) {
		String msg = message.getContentDisplay();
		String[] lines = msg.split("(\r\n|\n)");
		String[] fragments = SafeUtil.get(() -> lines[0].split("\\s+"));
		String encoded = SafeUtil.get(() -> fragments[fragments.length - 1].trim());
		return SafeUtil.get(() -> decodeReferenceCode(encoded.substring(1, encoded.length() - 1)));
	}

}
