package wp.discord.bot.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

public class CmdTokenTest {

	@Test
	public void shouldHaveUniqueCommandTokenKey() {
		Set<String> tokens = new HashSet<>();
		for (CmdToken t : CmdToken.values()) {
			Collection<String> cmds = t.getCmds();
			assertNotNull(cmds);

			if (CollectionUtils.isEmpty(cmds)) {
				throw new IllegalStateException("Empty Token: " + t);
			}

			cmds.forEach((cmd) -> {
				cmd = cmd.toLowerCase();
				if (tokens.contains(cmd)) {
					throw new IllegalStateException("Duplicated Token: " + t + ", cmd: " + cmd);
				}
				tokens.add(cmd);
			});
		}
	}

	@Test
	public void shouldAcceptItsOwnCommandTokenKey() {
		for (CmdToken t : CmdToken.values()) {
			Collection<String> cmds = t.getCmds();
			for (String cmd : cmds) {
				if (!t.accept(cmd)) {
					throw new IllegalStateException("Not Accepting Own token key: " + t + ", cmd: " + cmd);
				}
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
