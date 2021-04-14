package wp.discord.temp.core.action;

import java.lang.reflect.Method;

public interface ActionExecutionRoute {

	public Object getBean();

	public Method getMethod();

}
