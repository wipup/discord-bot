package wp.discord.bot.core.robot;

import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wp.discord.bot.model.CommandContext;

@Slf4j
@StateMachineParameters(stateType = String.class, eventType = String.class, contextType = CommandContext.class)
public class RobotCore extends AbstractUntypedStateMachine {

	private RobotHelper helper;
	private JDA jda;
	private GenericEvent jdaEvent;
	private Class<? extends GenericEvent> eventClass;

	protected void start(String from, String to, String event, CommandContext context) {
		log.debug("start, from={}, to={}, event={}, context={}", from, to, event, context);
		context.setJdaEvent(getJdaEvent());
		greet(from, to, event, context);
	}

	protected void logOut(String from, String to, String event, CommandContext context) {
		context.setAction(RobotAction.ACTION_LOG_OUT);
	}

	protected void greet(String from, String to, String event, CommandContext context) {
		log.debug("greet, from={}, to={}, event={}, context={}", from, to, event, context);
		if (!MessageReceivedEvent.class.isInstance(getJdaEvent())) {
			return;
		}

		MessageReceivedEvent e = (MessageReceivedEvent) jdaEvent;
		helper.setGreetAction(e.getAuthor(), e.getChannel(), context);
	}

	protected void setAction(String from, String to, String event, CommandContext context) {
		log.debug("setAction, from={}, to={}, event={}, context={}", from, to, event, context);
		context.setAction(RobotAction.ACTION_JOIN_VOICE_CHANNEL);
	}

	protected void setChannel(String from, String to, String event, CommandContext context) {
		log.debug("setChannel, from={}, to={}, event={}, context={}", from, to, event, context);
		if (!MessageReceivedEvent.class.isInstance(getJdaEvent())) {
			return;
		}

		MessageReceivedEvent e = (MessageReceivedEvent) getJdaEvent();
		VoiceChannel vc = helper.findAuthorVoiceChannel(e);
		if (vc != null) {
			helper.setVoiceChannel(vc, context);
		}

	}

	protected void setChannelById(String from, String to, String event, CommandContext context) {
		log.debug("setChannelById, from={}, to={}, event={}, context={}", from, to, event, context);
		String param = (String) context.getActionParam();

		if (param == null) {
			return;
		}
		VoiceChannel vc = getJda().getVoiceChannelById(param);
		if (vc != null) {
			helper.setVoiceChannel(vc, context);
		}
	}

	protected void end(String from, String to, String event, CommandContext context) {
		log.debug("end, from={}, to={}, event={}, context={}", from, to, event, context);
		helper.executeCommand(from, to, event, context);
	}

	@Override
	public void terminate(Object context) {
		log.debug("terminate internally: {}", context);
		super.terminate(context);
		log.debug("===========================");
	}

	public JDA getJda() {
		return jda;
	}

	protected void setJda(JDA jda) {
		this.jda = jda;
	}

	public GenericEvent getJdaEvent() {
		return jdaEvent;
	}

	protected void setJdaEvent(GenericEvent jdaEvent) {
		this.jdaEvent = jdaEvent;
	}

	public Class<? extends GenericEvent> getEventClass() {
		return eventClass;
	}

	protected void setEventClass(Class<? extends GenericEvent> eventClass) {
		this.eventClass = eventClass;
	}

	protected void setHelper(RobotHelper helper) {
		this.helper = helper;
	}

	public RobotHelper getHelper() {
		return helper;
	}
}
