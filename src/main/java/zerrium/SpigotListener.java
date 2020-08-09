package zerrium;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class SpigotListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        String name = p.getName();
        if(!Discord.zplayer.contains(new ZPlayer(uuid, ""))){
            Discord.zplayer.add(new ZPlayer(uuid, name));
            System.out.println(ChatColor.YELLOW + "[Stat2Discord]" + ChatColor.RESET + " Found a new player with uuid of " + uuid.toString() + " associates with " + name);
            System.out.println(ChatColor.YELLOW + "[Stat2Discord]" + ChatColor.RESET + " Added " + name + "to statistic player data.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        String uuid = p.getUniqueId().toString();
        String name = p.getName();
        System.out.println(ChatColor.YELLOW + "[Stat2Discord]" + ChatColor.RESET + name + " left the game. Updating stats...");
        ZPlayer zp = Discord.zplayer.get(Discord.zplayer.indexOf(new ZPlayer(UUID.fromString(uuid), name)));
        zp.updateStat();
        System.out.println(ChatColor.YELLOW + "[Stat2Discord]" + ChatColor.RESET + name + " stats has been updated");
    }
}
