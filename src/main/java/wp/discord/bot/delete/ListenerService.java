package wp.discord.bot.delete;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GatewayPingEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfMuteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class ListenerService extends ListenerAdapter {

	private static final String TEST = "\u0E17\u0E14\u0E2A\u0E2D\u0E1A";
	private static final String HELLO = "\u0E17\u0E14\u0E2A\u0E2D\u0E1A\u0E20\u0E32\u0E29\u0E32\u0E44\u0E17\u0E22";

	private JDA jda;
	private AudioSender sender;
	private boolean voiced = false;

	public ListenerService(JDA jda) {
		super();
		this.jda = jda;
		sender = new AudioSender(jda);
	}

	@Override
	public void onGenericEvent(GenericEvent event) {
		if (event instanceof GatewayPingEvent) {
			return;
		}
		System.out.println("Received Event: " + event.getClass().getSimpleName());
		System.out.println("              : " + event);
	}

	@Override
	public void onReady(ReadyEvent event) {
		System.out.println("API is ready!");
	}

	@Override
	public void onGuildVoiceSelfMute(GuildVoiceSelfMuteEvent event) {
		User user = event.getMember().getUser();
		VoiceChannel channel = event.getVoiceState().getChannel();
	}

	@Override
	public void onGuildVoiceMute(GuildVoiceMuteEvent event) {
	}

	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		User user = event.getEntity().getUser();
		VoiceChannel channel = event.getChannelJoined();
		Guild guild = event.getGuild();
		System.out.printf("[%s][%s] %#s: %s%n", guild.getName(), channel.getName(), user, "");
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}

		if (event.isFromType(ChannelType.TEXT)) {
			MessageChannel channel = event.getChannel();
			Message message = event.getMessage();
			String msg = message.getContentDisplay();

			System.out.printf("[%s][%s] %#s: %s%n", event.getGuild().getName(), channel.getName(), event.getAuthor(), msg);
			if (message.isMentioned(jda.getSelfUser(), MentionType.USER)) {
				replyMention(event);
			}

			if (msg.equalsIgnoreCase("/test")) {
				replyMention(event);
			} else if (msg.equalsIgnoreCase("/quit")) {
				event.getChannel().sendMessage("Disconnecting").queue();
				jda.shutdown();
			} else if (msg.replaceAll("\\s+", " ").toLowerCase().contains("hu tao")) {
				String userName = message.getAuthor().getName();

				event.getChannel().sendMessage(userName + "'s Waifu").queue();
			} else if (msg.contentEquals(TEST)) {
				event.getChannel().sendMessage(HELLO).queue();
			}

			else if (msg.contentEquals("/join")) {
				AudioManager audio = event.getGuild().getAudioManager();
				VoiceChannel vc = findChannel(event);
				if (vc == null) {
					System.out.println("VoiceChannel not found");
					return;
				}

				audio.setSendingHandler(sender);
				audio.openAudioConnection(vc);
//				event.getChannel().sendMessage("WTF!").queue();
				
				voiced = true;
			} else if (msg.contentEquals("/leave")) {
				voiced = false;
				
				AudioManager audio = event.getGuild().getAudioManager();
				audio.closeAudioConnection();
				
				
			} else if (msg.contentEquals("/play") && voiced) {
//				sender.getAudioPlayer().playTrack(Aud);
			}
		} else {
			User user = event.getAuthor();

			System.out.printf("[PM] %#s(%s): %s%n", user, user.getId(), event.getMessage().getContentDisplay());
			event.getChannel().sendMessage("Sorry, I am busy.").queue();
		}
	}

	private VoiceChannel findChannel(MessageReceivedEvent event) {
		try {
			VoiceChannel vc = event.getMember().getVoiceState().getChannel();
			return vc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void replyMention(MessageReceivedEvent event) {
		event.getChannel().sendMessage("Hello " + event.getAuthor().getName()).queue();
	}
}
