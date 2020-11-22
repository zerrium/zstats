package zerrium;

import github.scarsz.discordsrv.DiscordSRV;
import net.ess3.api.IUser;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class SpigotListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        String name = p.getName();
        Zstats.online_player.put(uuid, name);
        if(!Zstats.zplayer.contains(new ZPlayer(uuid))){
            Zstats.zplayer.add(new ZPlayer(uuid, name));
            System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Found a new player with uuid of " + uuid.toString() + " associates with " + name);
            BukkitRunnable r = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        PreparedStatement ps = Zstats.connection.prepareStatement("insert into player(uuid,name) values (?,?)");
                        ps.setString(1, uuid.toString());
                        ps.setString(2, name);
                        ps.executeUpdate();
                        ps.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            };
            r.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
            System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Added " + name + " to statistic player data.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        String name = p.getName();
        Zstats.online_player.remove(uuid);
        if(Zstats.debug) System.out.println(Zstats.online_player);
        System.out.println(ChatColor.YELLOW + "[Zstats] " + ChatColor.RESET + name + " left the game. Updating stats...");
        ZPlayer zp = Zstats.zplayer.get(Zstats.zplayer.indexOf(new ZPlayer(uuid)));
        zp.updateStat();
        System.out.println(ChatColor.YELLOW + "[Zstats] " + ChatColor.RESET + name + " stats has been updated");
        if(Zstats.notify_discord && Zstats.has_discordSrv){
            DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("global")
                    .sendMessage(Zstats.notify_discord_message.replaceAll("<player>".toLowerCase(), zp.name))
                    .queue();
        }
    }

    @EventHandler
    public void onPlayerAfkToggle(AfkStatusChangeEvent event){ //AFK stats not done
        IUser p = event.getAffected();
        if (!event.getValue()) { //back from AFK
            long x = (System.currentTimeMillis() - p.getAfkSince())/1000; //AFK time in seconds
            if(Zstats.debug) System.out.println("[Zstats] " + p.getName() + " has been AFK for " + x +" seconds.");
        }
    }
}
