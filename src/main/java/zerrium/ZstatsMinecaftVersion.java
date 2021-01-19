package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.LinkedHashMap;
import java.util.Map;

public class ZstatsMinecaftVersion {
    public static LinkedHashMap<String, Integer> versions = new LinkedHashMap<>();

    static{
        versions.put("1.8-", 0);
        versions.put("1.8.1", 0);
        versions.put("1.8.2", 0);

        //CHEST_OPENED and ITEM_ENCHANTED stat
        versions.put("1.8.3", 1);
        versions.put("1.8.4", 1);
        versions.put("1.8.5", 1);
        versions.put("1.8.6", 1);
        versions.put("1.8.7", 1);
        versions.put("1.8.8", 1);

        //AVIATE_ONE_CM (elytra distance) stat, SLEEP_IN_BED stat, SHIELD stat
        versions.put("1.9", 2);
        versions.put("1.10", 2);
        versions.put("1.11", 2);
        versions.put("1.12", 2);

        //Trident stat, PLAY_ONE_MINUTE on 1.13+ instead of PLAY_ONE_TICK on <1.13 only the name changes, it still records the tick actually
        versions.put("1.13", 3);

        //Crossbow stat
        versions.put("1.14", 4);

        //Supports OfflinePlayer#getStatistic
        versions.put("1.15", 5);
        versions.put("1.16", 5);
    }

    public static int getVersion(){
        String ver = Bukkit.getServer().getVersion();
        for(Map.Entry<String, Integer> me:versions.entrySet()){
            if(ver.contains(me.getKey())) return me.getValue();
        }
        System.out.println(ChatColor.YELLOW+"[Zstats] Warning! Your server version: " + ver + " might not be supported yet. Continue with your own precaution");
        return 5;
    }
}
