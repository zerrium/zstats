package zerrium;

import org.bukkit.ChatColor;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Discord {
    public static ArrayList<ZPlayer> zplayer;
    public static HashMap<UUID, String> online_player;
    public static long world_size = 0L;
    public static long nether_size = 0L;
    public static long end_size = 0L;
    public static long total_size = 0L;
    protected static TextChannel stat, general;
    private static DiscordApi api;

    public Discord(){
        if(SpigotEvent.debug) System.out.println("Javacord initiated");
        String botToken = SpigotEvent.fc.getString("bot_token");
        long channelID_stat = SpigotEvent.fc.getLong("stat_channel_id");
        long channelID_general = SpigotEvent.fc.getLong("general_channel_id");
        api = new DiscordApiBuilder().setToken(botToken).login().join();
        api.getServers().forEach(s ->{
            Optional<ServerTextChannel> i = s.getTextChannelById(channelID_stat);
            Optional<ServerTextChannel> j = s.getTextChannelById(channelID_general);
            if(SpigotEvent.debug) System.out.println(s.getName());
            if(i.isPresent()){
                stat = i.get();
                if(SpigotEvent.debug) System.out.println(i.get().toString());
            }
            if(j.isPresent()){
                general = j.get();
                if(SpigotEvent.debug) System.out.println(j.get().toString());
            }
        });
        api.addMessageCreateListener(event -> {
            if (event.getMessage().getContent().equalsIgnoreCase("!list")) {
                AtomicReference<String> content= new AtomicReference<>("No one is online");
                AtomicInteger i =new AtomicInteger(1);
                if(!online_player.isEmpty()){
                    content.set("Online players:\n");
                    online_player.forEach((k,v) ->{
                        content.getAndSet(content.get()+i.getAndIncrement()+". "+v+"\n");
                    });
                }
                event.getChannel().sendMessage(content.get());
            }
        });
        api.updateActivity("Zerrium Server");
        System.out.println(ChatColor.YELLOW+"[Stat2Discord] Discord connected");
        sendMsg(general, "Stats watcher has started");
    }

    public static void sendMsg(TextChannel tc, EmbedBuilder em, String text_top, String text_bottom){
        if(SpigotEvent.debug) System.out.println("Discord sendMsg() fired");
        new MessageBuilder()
                .append(text_top)
                .setEmbed(em)
                .append(text_bottom)
                .send(tc);
        if(SpigotEvent.debug) System.out.println("Discord sendMsg() done");
    }

    public static void sendMsg(TextChannel tc, String text){
        if(SpigotEvent.debug) System.out.println("Discord sendMsg() fired");
        new MessageBuilder()
                .append(text)
                .send(tc);
        if(SpigotEvent.debug) System.out.println("Discord sendMsg() done");
    }

    public static void sendMsg(TextChannel tc, EmbedBuilder em){
        if(SpigotEvent.debug) System.out.println("Discord sendMsg() fired");
        new MessageBuilder()
                .setEmbed(em)
                .send(tc);
        if(SpigotEvent.debug) System.out.println("Discord sendMsg() done");
    }

    public static void close(){
        sendMsg(general, "Stats watcher has stopped");
        api.disconnect();
        System.out.println(ChatColor.YELLOW+"[Stat2Discord] Discord disconnected");
    }
}
