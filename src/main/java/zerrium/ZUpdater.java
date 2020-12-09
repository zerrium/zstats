package zerrium;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public class ZUpdater implements CommandExecutor {
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
                                connection = SqlCon.openConnection();
                                for(ZPlayer p : Zstats.zplayer){
                                    p.updateStat(connection);
                                }
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            } finally {
                                try {
                                    assert connection != null;
                                    connection.close();
                                } catch (Exception e) {
                                    if(Zstats.debug) System.out.println("[Zstats] "+ e );
                                }
                            }
                            if(Zstats.notify_discord && Zstats.has_discordSrv){
                                DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("global")
                                        .sendMessage(Zstats.notify_discord_message.replaceAll("<player>".toLowerCase(), "all players"))
                                        .queue();
                            }
                        }
                    }
                };
                r.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
                return true;

            case 2: //update or delete specific player
                switch(args[0].toLowerCase()){
                    case "update":
                        BukkitRunnable rr = new BukkitRunnable() {
                            @Override
                            public void run() {
                                for(ZPlayer z : Zstats.zplayer){
                                    if(args[1].equalsIgnoreCase(z.name)){
                                        Connection connection = null;
                                        try {
                                            connection = SqlCon.openConnection();
                                            z.updateStat(connection);
                                            if(Zstats.notify_discord && Zstats.has_discordSrv){
                                                DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("global")
                                                        .sendMessage(Zstats.notify_discord_message.replaceAll("<player>".toLowerCase(), z.name))
                                                        .queue();
                                            }
                                        } catch (SQLException throwables) {
                                            throwables.printStackTrace();
                                        } finally {
                                            try {
                                                assert connection != null;
                                                connection.close();
                                            } catch (Exception e) {
                                                if(Zstats.debug) System.out.println("[Zstats] "+ e );
                                            }
                                        }
                                        return;
                                    }
                                }
                                sender.sendMessage(ChatColor.GOLD+"[Zstats]" + ChatColor.RESET + " Player " + args[1] + " was not found.");
                            }
                        };
                        rr.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
                        return true;

                    case "delete":
                        BukkitRunnable s = new BukkitRunnable() {
                            @Override
                            public void run() {
                                for(ZPlayer z : Zstats.zplayer){
                                    if(args[1].equalsIgnoreCase(z.name)){
                                        Connection connection = null;
                                        try {
                                            connection = SqlCon.openConnection();
                                            z.deleteStat(connection);
                                        } catch (SQLException throwables) {
                                            throwables.printStackTrace();
                                        } finally {
                                            try {
                                                assert connection != null;
                                                connection.close();
                                            } catch (Exception e) {
                                                if(Zstats.debug) System.out.println("[Zstats] "+ e );
                                            }
                                        }
                                        return;
                                    }
                                }
                                sender.sendMessage(ChatColor.GOLD+"[Zstats]" + ChatColor.RESET + " Player " + args[1] + " was not found.");
                            }
                        };
                        s.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
                        return true;

                    default:
                        sender.sendMessage(message);
                        return false;
                }

            default:
                sender.sendMessage(message);
                return false;
        }
    }
}
