package zerrium;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.message.*;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.listener.user.UserStartTypingListener;
import org.javacord.api.util.cache.MessageCache;
import org.javacord.api.util.event.ListenerManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Discord {
    public static ArrayList<ZPlayer> zplayer;
    public static long world_size = 0L;
    public static long nether_size = 0L;
    public static long end_size = 0L;
    public static long total_size = 0L;
    private String botToken;
    private long channelID;
    private DiscordApi api;

    public Discord(){
        if(SpigotEvent.debug) System.out.println("Javacord initiated");
        botToken = SpigotEvent.fc.getString("bot_token");
        channelID = SpigotEvent.fc.getLong("channel_id");
        api = new DiscordApiBuilder().setToken(botToken).login().join();
        api.updateActivity("Zerrium Server");
        if(SpigotEvent.debug) System.out.println("Javacord connected");
        sendMsg();

        /* Add a listener which answers with "Pong!" if someone writes "!ping"
        api.addMessageCreateListener(event -> {
            if (event.getMessage().getContent().equalsIgnoreCase("!ping")) {
                event.getChannel().sendMessage("Pong!");
            }
        });
        */
    }

    public void sendMsg(){
        if(SpigotEvent.debug) System.out.println("Discord sendMsg() fired");
        new MessageBuilder()
                .append("This is BOT testing sent from Zerrium's Spigot plugin")
                .setEmbed(new EmbedBuilder()
                        .setTitle("UWAW it works :v")
                        .setDescription("Ninu ninu ninu... (Zerrium has gone NUTS)")
                        .setColor(Color.ORANGE))
                .append("Haha coding go bbbrrrrrrrrrrr!!!")
                .send(api.getTextChannelById(channelID).get());
        if(SpigotEvent.debug) System.out.println("Discord sendMsg() done");
    }
}
