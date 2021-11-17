package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import zerrium.configs.ZstatsConfigs;
import zerrium.configs.ZstatsMinecaftVersion;
import zerrium.listeners.ZstatsEssentialsListener;
import zerrium.listeners.ZstatsListener;
import zerrium.utils.ZstatsFilter;
import zerrium.utils.ZstatsGeneralUtils;
import zerrium.utils.ZstatsSqlUtil;
import zerrium.utils.ZstatsUpdater;

import java.sql.*;
import java.util.*;

public class Zstats extends JavaPlugin{
    private static int version;
    private static boolean hasDiscordSrv, hasEssentials;

    private Connection connection;

    @Override
    public void onEnable() {
        System.out.println(ChatColor.YELLOW+"[Zstats] v3.0 by zerrium");
        getServer().getPluginManager().registerEvents(new ZstatsListener(), this);
        Objects.requireNonNull(this.getCommand("zstats")).setExecutor(new ZstatsUpdater());
        Objects.requireNonNull(getCommand("zstats")).setTabCompleter(this);
        version = ZstatsMinecaftVersion.getVersion();

        this.saveDefaultConfig(); //get or create config file

        //Loads zstats configs
        new ZstatsConfigs();

        System.out.println(ChatColor.YELLOW+"[Zstats] Connecting to MySQL database...");

        //MySQL connect
        try{
            connection = ZstatsSqlUtil.openConnection();
        } catch (SQLException throwables) {
            System.out.println(ChatColor.YELLOW+"[Zstats]"+ChatColor.RED+" Unable to connect to database:");
            throwables.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
        ZstatsGeneralUtils.init(connection);

        //Check optional plugin dependencies
        if(Bukkit.getPluginManager().getPlugin("DiscordSRV") != null || Bukkit.getPluginManager().getPlugin("discordsrv") != null){
            System.out.println(ChatColor.YELLOW+"[Zstats] DiscordSRV plugin detected. Messaging system to DiscordSRV is hooked.");
            hasDiscordSrv = true;
        }else{
            System.out.println(ChatColor.YELLOW+"[Zstats] No DiscordSRV plugin detected. Disabled messaging system to DiscordSRV. ");
            hasDiscordSrv = false;
        }
        if(Bukkit.getPluginManager().getPlugin("Essentials") != null || Bukkit.getPluginManager().getPlugin("EssentialsX") != null){
            getServer().getPluginManager().registerEvents(new ZstatsEssentialsListener(), this);
            System.out.println(ChatColor.YELLOW+"[Zstats] Essentials plugin detected. AFK detection for AFK time stats enabled.");
            hasEssentials = true;
        }else{
            System.out.println(ChatColor.YELLOW+"[Zstats] No Essentials plugin detected. Disabled AFK time stats");
            hasEssentials = false;
        }
        ZstatsFilter.begin();
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
        if(command.getName().equals("zstats")){
            return switch (args.length) {
                case 1 -> Arrays.asList("update", "delete");
                case 2 -> switch (args[0]) {
                    case "update", "delete", "remove" -> null; //auto complete online players
                    default -> Collections.emptyList();
                };
                default -> Collections.emptyList();
            };
        }else{
            return Collections.emptyList();
        }
    }

    @Override
    public void onDisable() {
        ZstatsSqlUtil.closeConnection();
        System.out.println(ChatColor.YELLOW+"[Zstats] Disabling plugin...");
    }

    public static boolean getHasDiscordSrv() {
        return hasDiscordSrv;
    }

    public static boolean getHasEssentials() {
        return hasEssentials;
    }

    public static int getVersion() {
        return version;
    }
}