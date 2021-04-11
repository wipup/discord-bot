package wp.discord.bot.core.action;

import java.lang.reflect.Method;

public interface ActionExecutionRoute {

	public Object getBean();

	public Method getMethod();

}
