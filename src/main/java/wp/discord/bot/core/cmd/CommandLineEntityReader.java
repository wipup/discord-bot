package wp.discord.bot.core.cmd;

import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.model.BotAction;

public interface CommandLineEntityReader {

	public int collectEntityOption(BotAction action, String[] tokens, int currentIndex, CmdEntity entity);

	public CmdEntity accepEntity();
}
