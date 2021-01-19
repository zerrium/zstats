package zerrium;

import github.scarsz.discordsrv.DiscordSRV;
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

public class ZstatsListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        String name = p.getName();
        Zstats.online_player.put(uuid, name);
        if(!Zstats.zplayer.contains(new ZstatsPlayer(uuid))){
            BukkitRunnable r = new BukkitRunnable() {
                @Override
                public void run() {
                    Connection connection = null;
                    PreparedStatement ps = null;
                    try {
                        Zstats.zplayer.add(new ZstatsPlayer(uuid, name));
                        System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Found a new player with uuid of " + uuid.toString() + " associates with " + name);
                        connection = ZstatsSqlCon.openConnection();
                        ps = connection.prepareStatement("insert into player(uuid,name) values (?,?)");
                        ps.setString(1, uuid.toString());
                        ps.setString(2, name);
                        ps.executeUpdate();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();

                    } finally {
                        try {
                            assert ps != null;
                            ps.close();
                            connection.close();
                        } catch (Exception e) {
                            if(Zstats.debug) System.out.println("[Zstats] "+ e );
                        }
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
        if (Zstats.version < 5) ZstatsPlayer.players.add(new ZstatsOldPlayer(p));
        UUID uuid = p.getUniqueId();
        String name = p.getName();
        Zstats.online_player.remove(uuid);
        if(Zstats.debug) System.out.println(Zstats.online_player);
        System.out.println(ChatColor.YELLOW + "[Zstats] " + ChatColor.RESET + name + " left the game. Updating stats...");
        ZstatsPlayer zp = Zstats.zplayer.get(Zstats.zplayer.indexOf(new ZstatsPlayer(uuid)));
        if(zp.is_updating) return;
        zp.last_played = System.currentTimeMillis()/1000;
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                Connection connection = null;
                try {
                    connection = ZstatsSqlCon.openConnection();
                    zp.updateStat(connection);
                    if(Zstats.notify_discord && Zstats.has_discordSrv){
                        DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("global")
                                .sendMessage(Zstats.notify_discord_message.replaceAll("<player>".toLowerCase(), name))
                                .queue();
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    zp.is_updating = false;
                } finally {
                    try {
                        assert connection != null;
                        connection.close();
                    } catch (Exception e) {
                        if(Zstats.debug) System.out.println("[Zstats] "+ e );
                    }
                }
            }
        };
        r.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
    }
}
