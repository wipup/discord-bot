package wp.discord.bot.task.reaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import wp.discord.bot.constant.Reaction;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.model.Reference;
import wp.discord.bot.task.audio.AudioListReference;
import wp.discord.bot.task.get.GetAudioTask;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
public class GetAudioReactionTask {

	@Autowired
	private TracingHandler tracing;

	@Autowired
	private GetAudioTask getAudioTask;

	public void handleAction(GenericMessageReactionEvent event, BotAction action, Message message, Reference ref) throws Exception {
		ReactionEmote emote = event.getReactionEmote();

		Reaction reaction = Reaction.getReaction(emote);
		AudioListReference audioList = SafeUtil.get(() -> AudioListReference.construct(ref));
		if (audioList == null) {
			return;
		}

		audioList = processReaction(audioList, reaction);
		audioList = getAudioTask.getAudios(action, audioList);

		Reply reply = audioList.reply();
		tracing.queue(message.editMessage(reply.build()));
	}

	public AudioListReference processReaction(AudioListReference list, Reaction reaction) {
		int offset = list.getOffset();
		if (reaction == Reaction.LEFT) {
			offset -= list.getMaxDisplaySize();

		} else if (reaction == Reaction.RIGHT) {
			offset += list.getMaxDisplaySize();

		}

		if (offset < 0) {
			offset = 0;
		}
		if (offset >= list.getTotal()) {
			offset = list.getTotal();
		}
		list.setOffset(offset);
		return list;
	}
}
