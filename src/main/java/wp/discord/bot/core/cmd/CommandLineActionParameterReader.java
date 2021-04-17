package wp.discord.bot.core.cmd;

import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.model.BotAction;

public interface CommandLineActionParameterReader {

	public int collectActionParams(BotAction action, String[] tokens, int currentIndex, CmdAction cmdAction); 

	public CmdAction acceptActionParam();
}
