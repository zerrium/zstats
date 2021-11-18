package zerrium.configs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.LinkedHashMap;
import java.util.Map;

public class ZstatsMinecaftVersion {
    public final static LinkedHashMap<String, Integer> versions = new LinkedHashMap<>(Map.ofEntries(
                Map.entry("1.8-", 0),
                Map.entry("1.8.1", 0),
                Map.entry("1.8.2", 0),

                //CHEST_OPENED and ITEM_ENCHANTED stat
                Map.entry("1.8.3", 1),
                Map.entry("1.8.4", 1),
                Map.entry("1.8.5", 1),
                Map.entry("1.8.6", 1),
                Map.entry("1.8.7", 1),
                Map.entry("1.8.8", 1),

                //AVIATE_ONE_CM (elytra distance) stat, SLEEP_IN_BED stat, SHIELD stat
                Map.entry("1.9", 2),
                Map.entry("1.10", 2),
                Map.entry("1.11", 2),
                Map.entry("1.12", 2),

                //Trident stat, PLAY_ONE_MINUTE on 1.13+ instead of PLAY_ONE_TICK on <1.13 only the name changes, it still records the tick actually
                Map.entry("1.13", 3),

                //Crossbow stat
                Map.entry("1.14", 4),

                //Supports OfflinePlayer#getStatistic
                Map.entry("1.15", 5),
                Map.entry("1.16", 5),

                //SPYGLASS tool filter
                Map.entry("1.17", 6)
        ));

    public static int getVersion(){
        String ver = Bukkit.getServer().getVersion();
        for(Map.Entry<String, Integer> me:versions.entrySet()){
            if(ver.contains(me.getKey())) return me.getValue();
        }
        System.out.println(ChatColor.YELLOW+"[Zstats] Warning! Your server version: " + ver + " might not be supported yet. Continue with your own precaution");
        return 6;
    }
}
