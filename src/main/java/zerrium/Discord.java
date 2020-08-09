package zerrium;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.ArrayList;

public class Discord {
    public static ArrayList<ZPlayer> zplayer;
    public static long world_size = 0L;
    public static long nether_size = 0L;
    public static long end_size = 0L;
    public static long total_size = 0L;

    public void Discord() {
        String botToken = SpigotEvent.fc.getString("bot_token");//"NzA3NTM3NDQ3NTMzMjgxMzUy.XrKPmA.HVzarNFEC3Dx5LOe8PMGk-Zcb3o";
        String channelID = SpigotEvent.fc.getString("channel_id");//"704643254469001257";

        DiscordApi api = new DiscordApiBuilder().setToken(botToken).login().join();
        api.updateActivity("Reading minecraft statistics");
        /* Add a listener which answers with "Pong!" if someone writes "!ping"
        api.addMessageCreateListener(event -> {
            if (event.getMessage().getContent().equalsIgnoreCase("!ping")) {
                event.getChannel().sendMessage("Pong!");
            }
        });
        */
    }
}
