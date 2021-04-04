package wp.discord.bot.core.cmd;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.matcher.StringMatcher;
import org.apache.commons.text.matcher.StringMatcherFactory;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandBuilder {

	static String[] template = new String[] {
			"start.bot", ""
	};
	

	public static void main(String[] args) {
		String cmd = "bot  join    here";
		String[] cmdFrags = cmd.split(" ");


		
		log.info("{}", (Object) cmdFrags);

		final Map<String, Object> valueMap = new HashMap<String, Object>();
		valueMap.put("username", "Wipu");
		valueMap.put("id", "12345");
		
		StringSubstitutor sub = new StringSubstitutor(valueMap);
		sub.setEnableSubstitutionInVariables(true);
		
		String result = sub.replace("Hello ${username}, your id is ${id}");
		System.out.println(result);
		
		
		StringMatcher matcher = StringMatcherFactory.INSTANCE.charSetMatcher("username");
		int pos = matcher.isMatch("Hello ${username}, your id is ${id}", 0);
		System.out.println(pos);
		
		Map<String, Object> ctx = new HashMap<>();
		
		UntypedStateMachine bot = initMachine();
		bot.fire("action.join", ctx);
		bot.fire("join.here", ctx);
		bot.fire("action.join.channel", ctx);
		
		System.out.println("UntypedStateMachine: " + bot);
	}
	
	// root -> 
	

	//https://github.com/hekailiang/squirrel
	
	// 2. Define State Machine Class
	@StateMachineParameters(stateType = String.class, eventType = String.class, contextType = Map.class)
	static class PaimonBot extends AbstractUntypedStateMachine {
		
		protected void initContext(String from, String to, String event, Map<String, String> context) {
			context.put("user", "userId:5555");
		}
		
		protected void setChannel(String from, String to, String event, Map<String, String> context) {
			context.put("channel.id", "channelId:1234");
		}
		
		protected void setChannelFromUser(String from, String to, String event, Map<String, String> context) {
			context.put("channel.id", "channelId of userId");
		}
		
		protected void joinChannel(String from, String to, String event, Map<String, String> context) {
			if (event.equals("here")) {
				
			} else if (event.equals("channel")) {
				
			}
		}
		
		
	}

	
	public static UntypedStateMachine initMachine() {
		UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(PaimonBot.class);
		
		builder.transition().from("start").to("join.what").on("join");
		builder.transition().from("join.what").to("channel").on("channel");
		builder.transition().from("channel").to("do.join.channel").on("action.join.channel");
		
		builder.transition().from("join.what").to("do.join.channel").on("here").callMethod("setChannelFromUser");
		
		builder.onExit("start").callMethod("initContext");
		builder.onEntry("channel").callMethod("setChannel");
		builder.onEntry("do.join.channel").callMethod("joinChannel");
		
		// 3. Build State Transitions
//		UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(PaimonBot.class);
////		builder.externalTransition().from("A").to("B").on(PaimonThought.TASK).callMethod("fromAToB");
//		builder.onEntry("B").callMethod("ontoB");
//
//		// 4. Use State Machine
		UntypedStateMachine fsm = builder.newStateMachine("start");
		return fsm;
//		fsm.fire(FSMEvent.ToB, 10);
//
//		System.out.println("Current state is " + fsm.getCurrentState());
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
