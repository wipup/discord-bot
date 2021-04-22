package wp.discord.bot.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class CmdTokenTest {

	@Test
	public void shouldHaveUniqueCommandTokenKey() {
		Set<String> tokens = new HashSet<>();
		for (CmdToken t : CmdToken.values()) {
			Collection<String> cmds = t.getCmds();
			assertNotNull(cmds);
			assertFalse(cmds.isEmpty(), "Empty Token: " + t);

			cmds.forEach((cmd) -> {
				cmd = cmd.toLowerCase();

				assertFalse(tokens.contains(cmd), "Duplicated Token: " + t + ", cmd: " + cmd);
				tokens.add(cmd);
			});
		}
	}

	@Test
	public void shouldAcceptItsOwnCommandTokenKey() {
		for (CmdToken t : CmdToken.values()) {
			Collection<String> cmds = t.getCmds();
			for (String cmd : cmds) {
				assertTrue(t.accept(cmd), "Not Accepting Own token key: " + t + ", cmd: " + cmd);
			}
		}
	}

	@Test
	public void shouldGetMatchingTokenKey() {
		for (CmdToken t : CmdToken.values()) {
			Collection<String> cmds = t.getCmds();
			for (String cmd : cmds) {
				CmdToken token = CmdToken.getMatchingCmdToken(cmd);
				assertEquals(token, t, "Not getting matching CmdToken");
			}
		}
	}
}
