package zerrium;

import net.ess3.api.IUser;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
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
                        Connection connection = new SqlCon().openConnection();
                        PreparedStatement ps = connection.prepareStatement("insert into player(uuid,name) values (?,?)");
                        ps.setString(1, uuid.toString());
                        ps.setString(2, name);
                        ps.executeUpdate();
                        ps.close();
                        connection.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                    System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Added " + name + " to statistic player data.");
                }
            };
            r.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
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
        try {
            Connection connection = new SqlCon().openConnection();
            zp.updateStat(connection);
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerAfkToggle(AfkStatusChangeEvent event){
        if(Zstats.hasEssentials){
            IUser p = event.getAffected();
            if (!event.getValue()) { //back from AFK
                long x = (System.currentTimeMillis() - p.getAfkSince())/1000; //AFK time in seconds
                ZPlayer zp = Zstats.zplayer.get(Zstats.zplayer.indexOf(new ZPlayer(p.getBase().getUniqueId())));
                zp.afk_time += x;
                if(Zstats.debug) System.out.println("[Zstats] " + p.getName() + " has been AFK for " + x +" seconds.");
            }
        }
    }
}
