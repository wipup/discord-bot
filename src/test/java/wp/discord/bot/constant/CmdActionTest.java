package wp.discord.bot.constant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CmdActionTest {

	@Test
	public void shouldHaveUniqueCommandTokenKey() {
		Set<String> tokens = new HashSet<>();
		for (CmdAction t : CmdAction.values()) {
			String cmd = t.getCmd();
			assertNotNull(cmd);
			Assertions.assertFalse(StringUtils.isBlank(cmd), "Empty Token: " + t);

			cmd = cmd.toLowerCase();
			Assertions.assertFalse(tokens.contains(cmd), "Duplicated Token: " + t + ", cmd: " + cmd);
			tokens.add(cmd);
		}
	}

	@Test
	public void shouldAcceptItsOwnCommandTokenKey() {
		for (CmdAction t : CmdAction.values()) {
			String cmd = t.getCmd();
			assertTrue(t.accept(cmd), "Not Accepting Own token key: " + t + ", cmd: " + cmd);
		}
	}

	@Test
	public void shouldGetMatchingTokenKey() {
		for (CmdAction t : CmdAction.values()) {
			String cmd = t.getCmd();
			CmdAction token = CmdAction.getMatchingAction(cmd);
			assertSame(token, t, "Not getting matching Action");
		}
	}
}
