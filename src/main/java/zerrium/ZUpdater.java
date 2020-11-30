package zerrium;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public class ZUpdater implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final String message = ChatColor.GOLD + "[Zstats]" + ChatColor.RESET + " usage:\n" +
                ChatColor.GOLD + "/zstats update" + ChatColor.RESET + " Update all player stats to database (may drop server's performance)" +
                ChatColor.GOLD + "/zstats update <player>" + ChatColor.RESET + " Update specified player stats to database" +
                ChatColor.GOLD + "/zstats delete <player>" + ChatColor.RESET + " Delete specified player stats from database";
        switch(args.length){
            case 0:
                sender.sendMessage(message);
                return true;

            case 1: //update all
                if(args[0].equalsIgnoreCase("update")){
                    sender.sendMessage(ChatColor.GOLD+"[Zstats]" + ChatColor.RESET + " updating stats for all player...");
                    try {
                        Connection connection = new SqlCon().openConnection();
                        for(ZPlayer p : Zstats.zplayer){
                            p.updateStat(connection);
                        }
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
                    if(Zstats.notify_discord && Zstats.has_discordSrv){
                        DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("global")
                                .sendMessage(Zstats.notify_discord_message.replaceAll("<player>".toLowerCase(), "all players"))
                                .queue();
                    }
                    return true;
                }
                break;

            case 2: //update or delete specific player
                switch(args[0].toLowerCase()){
                    case "update":
                        for(ZPlayer z : Zstats.zplayer){
                            if(args[1].equalsIgnoreCase(z.name)){
                                try {
                                    Connection connection = new SqlCon().openConnection();
                                    z.updateStat(connection);
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
                                return true;
                            }
                        }
                        sender.sendMessage(ChatColor.GOLD+"[Zstats]" + ChatColor.RESET + " Player " + args[1] + " was not found.");
                        return true;

                    case "delete":
                        for(ZPlayer z : Zstats.zplayer){
                            if(args[1].equalsIgnoreCase(z.name)){
                                try {
                                    Connection connection = new SqlCon().openConnection();
                                    z.deleteStat(connection);
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
                                return true;
                            }
                        }
                        sender.sendMessage(ChatColor.GOLD+"[Zstats]" + ChatColor.RESET + " Player " + args[1] + " was not found.");
                        return true;
                }
                break;

            default:
                sender.sendMessage(message);
                return false;
        }
        sender.sendMessage(message);
        return false;
    }
}
