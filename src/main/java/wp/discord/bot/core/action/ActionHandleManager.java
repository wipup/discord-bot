package wp.discord.bot.core.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.constant.CmdAction;
import wp.discord.bot.exception.BotException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;

@Slf4j
@Component
public class ActionHandleManager implements InitializingBean {

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	private Map<CmdAction, List<ActionHandler>> actionHandlerMap;

	public void executeAction(BotAction action) throws Exception {
		CmdAction cmd = action.getAction();
		if (cmd == null) {
			cmd = CmdAction.GREET;
		}

		List<ActionHandler> handlers = actionHandlerMap.get(cmd);
		if (handlers == null) {
			log.error("Unsupported Action={}, {}", cmd, action);
			throw new BotException(Reply.of().literal("Unsupported action: ").code(cmd.getCmd()).newline() //
					.mentionUser(action.getAuthorId()).literal(" Please try again"));
		}

		for (ActionHandler h : handlers) {
			h.handleAction(action);
		}
	}

	// ------------------------------------------------------------------------

	@Override
	public void afterPropertiesSet() throws Exception {
		Collection<ActionHandler> beans = applicationContext.getBeansOfType(ActionHandler.class).values();
		registerActionExecutors(beans);
	}

	private void registerActionExecutors(Collection<ActionHandler> allHandlers) {
		Map<CmdAction, List<ActionHandler>> handlers = new HashMap<>();
		for (ActionHandler bean : allHandlers) {

			CmdAction action = bean.getAcceptedAction();
			Objects.requireNonNull(action);

			List<ActionHandler> handlerList = handlers.get(action);
			if (handlerList == null) {
				handlerList = new ArrayList<>();
				handlers.put(action, handlerList);
			}

			handlerList.add(bean);

		}

		actionHandlerMap = Collections.unmodifiableMap(handlers);
	}

}
