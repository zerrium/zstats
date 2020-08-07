package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.UUID;

public class SpigotEvent extends JavaPlugin{
    @Override
    public void onEnable() {
        System.out.println(ChatColor.YELLOW+"[Stat2Discord] v0.1 by zerrium");
        System.out.println(ChatColor.YELLOW+"[Stat2Discord] Getting player lists...");
        Discord.zplayer = new ArrayList<>();
        int counter = 0;
        for(OfflinePlayer i: Bukkit.getOfflinePlayers()){
            if(i.hasPlayedBefore()) {
                counter++;
                UUID uuid = i.getUniqueId();
                String name = i.getName();
                System.out.println(ChatColor.YELLOW + "[Stat2Discord]" + ChatColor.RESET + " Found player with uuid of " + uuid.toString() + " associates with " + name);
                Discord.zplayer.add(new ZPlayer(uuid, name));
                int a = i.getStatistic(Statistic.AVIATE_ONE_CM);
            }
        }
        System.out.println(ChatColor.YELLOW+"[Stat2Discord] Enabling Discord asynchronously...");
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                new Discord();
                new ZFilter();
            }
        };
        r.runTaskAsynchronously(this);
        System.out.println(ChatColor.YELLOW+"[Stat2Discord] Found statistic data of "+ counter +" players.");
    }

    @Override
    public void onDisable() {
        System.out.println(ChatColor.YELLOW+"[Stat2Discord] v0.1 disabling plugin");
    }
}