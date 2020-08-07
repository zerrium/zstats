package zerrium;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.ArrayList;

public class Discord {
    public static ArrayList<ZPlayer> zplayer;

    public void Discord() {
        String botToken = "NzA3NTM3NDQ3NTMzMjgxMzUy.XrKPmA.HVzarNFEC3Dx5LOe8PMGk-Zcb3o";
        String channelID = "704643254469001257";

        DiscordApi api = new DiscordApiBuilder().setToken(botToken).login().join();
        api.updateActivity("Reading minecraft statistics");
        /* Add a listener which answers with "Pong!" if someone writes "!ping"
        api.addMessageCreateListener(event -> {
            if (event.getMessage().getContent().equalsIgnoreCase("!ping")) {
                event.getChannel().sendMessage("Pong!");
            }
        });
        */
        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }
}
