package zerrium.utils;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import zerrium.Zstats;
import zerrium.configs.ZstatsConfigs;
import zerrium.models.ZstatsConfig;
import zerrium.models.ZstatsPlayer;

import java.sql.Connection;
import java.sql.SQLException;

public class ZstatsUpdater implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final String message = ChatColor.GOLD + "[Zstats]" + ChatColor.RESET + " usage:\n" +
                ChatColor.GOLD + "/zstats update" + ChatColor.RESET + " Update all player stats to database (may takes long to finish)\n" +
                ChatColor.GOLD + "/zstats update <player>" + ChatColor.RESET + " Update specified player stats to database\n" +
                ChatColor.GOLD + "/zstats delete <player>" + ChatColor.RESET + " Delete specified player stats from database\n";
        switch(args.length){
            case 0:
                sender.sendMessage(message);
                return true;

            case 1: //update all
                BukkitRunnable r = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(args[0].equalsIgnoreCase("update")){
                            sender.sendMessage(ChatColor.GOLD+"[Zstats]" + ChatColor.RESET + " updating stats for all player...");
                            Connection connection = null;
                            try {
                                connection = ZstatsSqlUtil.openConnection();
                                for(ZstatsPlayer p : ZstatsGeneralUtils.getZplayer()){
                                    if(p.is_updating) continue;
                                    if(Zstats.getVersion() < 5 && !Bukkit.getOfflinePlayer(p.uuid).isOnline()) continue; //update only when the player is online on version <1.15
                                    p.updateStat(connection);
                                }
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            } finally {
                                try {
                                    assert connection != null;
                                    connection.close();
                                } catch (Exception e) {
                                    if(ZstatsConfigs.getDebug()) System.out.println("[Zstats] "+ e );
                                }
                            }
                            if(ZstatsConfigs.getBooleanConfig(ZstatsConfig.NOTIFY_DISCORD) && Zstats.getHasDiscordSrv()){
                                DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("global")
                                        .sendMessage(ZstatsConfigs.getStringConfig(ZstatsConfig.DISCORD_MESSAGE).replaceAll("<player>".toLowerCase(), "all players"))
                                        .queue();
                            }
                        }
                    }
                };
                r.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
                return true;

            case 2: //update or delete specific player
                switch (args[0].toLowerCase()) {
                    case "update" -> {
                        BukkitRunnable rr = new BukkitRunnable() {
                            @Override
                            public void run() {
                                for (ZstatsPlayer z : ZstatsGeneralUtils.getZplayer()) {
                                    if (args[1].equalsIgnoreCase(z.name)) {
                                        if (z.is_updating) return;
                                        if (Zstats.getVersion() < 5 && !Bukkit.getOfflinePlayer(z.uuid).isOnline()) { //update only when the player is online on version <1.15
                                            sender.sendMessage(ChatColor.GOLD + "[Zstats]" + ChatColor.RESET + " Can't update stats of player " + args[1] + " as he is offline.");
                                            return;
                                        }
                                        Connection connection = null;
                                        try {
                                            connection = ZstatsSqlUtil.openConnection();
                                            z.updateStat(connection);
                                            if (ZstatsConfigs.getBooleanConfig(ZstatsConfig.NOTIFY_DISCORD) && Zstats.getHasDiscordSrv()) {
                                                DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("global")
                                                        .sendMessage(ZstatsConfigs.getStringConfig(ZstatsConfig.DISCORD_MESSAGE).replaceAll("<player>".toLowerCase(), z.name))
                                                        .queue();
                                            }
                                        } catch (SQLException throwables) {
                                            throwables.printStackTrace();
                                        } finally {
                                            try {
                                                assert connection != null;
                                                connection.close();
                                            } catch (Exception e) {
                                                if (ZstatsConfigs.getDebug()) System.out.println("[Zstats] " + e);
                                            }
                                        }
                                        return;
                                    }
                                }
                                sender.sendMessage(ChatColor.GOLD + "[Zstats]" + ChatColor.RESET + " Player " + args[1] + " was not found.");
                            }
                        };
                        rr.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
                        return true;
                    }
                    case "remove", "delete" -> {
                        BukkitRunnable s = new BukkitRunnable() {
                            @Override
                            public void run() {
                                for (ZstatsPlayer z : ZstatsGeneralUtils.getZplayer()) {
                                    if (args[1].equalsIgnoreCase(z.name)) {
                                        Connection connection = null;
                                        try {
                                            connection = ZstatsSqlUtil.openConnection();
                                            z.deleteStat(connection);
                                        } catch (SQLException throwables) {
                                            throwables.printStackTrace();
                                        } finally {
                                            try {
                                                assert connection != null;
                                                connection.close();
                                            } catch (Exception e) {
                                                if (ZstatsConfigs.getDebug()) System.out.println("[Zstats] " + e);
                                            }
                                        }
                                        return;
                                    }
                                }
                                sender.sendMessage(ChatColor.GOLD + "[Zstats]" + ChatColor.RESET + " Player " + args[1] + " was not found.");
                            }
                        };
                        s.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
                        return true;
                    }
                    default -> {
                        sender.sendMessage(message);
                        return false;
                    }
                }

            default:
                sender.sendMessage(message);
                return false;
        }
    }
}
