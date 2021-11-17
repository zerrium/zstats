package zerrium.listeners;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import zerrium.Zstats;
import zerrium.models.ZstatsOldPlayer;
import zerrium.models.ZstatsPlayer;
import zerrium.configs.ZstatsConfigs;
import zerrium.models.ZstatsConfig;
import zerrium.utils.ZstatsGeneralUtils;
import zerrium.utils.ZstatsSqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ZstatsListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        String name = p.getName();
        ZstatsGeneralUtils.getOnlinePlayer().put(uuid, name);

        final ArrayList<ZstatsPlayer> zplayer = ZstatsGeneralUtils.getZplayer();
        if(!zplayer.contains(new ZstatsPlayer(uuid))){
            BukkitRunnable r = new BukkitRunnable() {
                @Override
                public void run() {
                    Connection connection = null;
                    PreparedStatement ps = null;
                    try {
                        zplayer.add(new ZstatsPlayer(uuid, name));
                        System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Found a new player with uuid of " + uuid.toString() + " associates with " + name);
                        connection = ZstatsSqlUtil.openConnection();
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
                            if(ZstatsConfigs.getDebug()) System.out.println("[Zstats] "+ e );
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
        final boolean debug = ZstatsConfigs.getDebug();
        final ArrayList<ZstatsPlayer> zplayer = ZstatsGeneralUtils.getZplayer();
        final HashMap<UUID, String> onlinePlayer = ZstatsGeneralUtils.getOnlinePlayer();

        Player p = event.getPlayer();
        if (Zstats.getVersion() < 5) ZstatsPlayer.players.add(new ZstatsOldPlayer(p));
        UUID uuid = p.getUniqueId();
        String name = p.getName();
        onlinePlayer.remove(uuid);
        if(debug) System.out.println(onlinePlayer);
        System.out.println(ChatColor.YELLOW + "[Zstats] " + ChatColor.RESET + name + " left the game. Updating stats...");
        ZstatsPlayer zp = zplayer.get(zplayer.indexOf(new ZstatsPlayer(uuid)));
        if(zp.is_updating) return;
        zp.last_played = System.currentTimeMillis()/1000;
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                Connection connection = null;
                try {
                    connection = ZstatsSqlUtil.openConnection();
                    zp.updateStat(connection);
                    if(ZstatsConfigs.getBooleanConfig(ZstatsConfig.NOTIFY_DISCORD) && Zstats.getHasDiscordSrv()){
                        DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("global")
                                .sendMessage(ZstatsConfigs.getStringConfig(ZstatsConfig.DISCORD_MESSAGE).replaceAll("<player>".toLowerCase(), name))
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
                        if(debug) System.out.println("[Zstats] "+ e );
                    }
                }
            }
        };
        r.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
    }
}
