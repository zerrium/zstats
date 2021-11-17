package zerrium.configs;

import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import zerrium.Zstats;
import zerrium.models.ZstatsConfig;
import zerrium.utils.ZstatsGeneralUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ZstatsConfigs {
    private static FileConfiguration fc;
    private static HashMap<ZstatsConfig, Boolean> booleanConfigs;
    private static HashMap<ZstatsConfig, Integer> intConfigs;
    private static HashMap<ZstatsConfig, String> stringConfigs;
    private static HashMap<String, Boolean> zstats, vanillaStats;

    private static boolean debug;
    private boolean is_writing_config = false;

    public static boolean getBooleanConfig(ZstatsConfig config) {
        return booleanConfigs.get(config);
    }

    public static int getIntConfig(ZstatsConfig config) {
        return intConfigs.get(config);
    }

    public static String getStringConfig(ZstatsConfig config) {
        return stringConfigs.get(config);
    }

    public static HashMap<String, Boolean> getZstats() {
        return zstats;
    }

    public static HashMap<String, Boolean> getVanillaStats() {
        return vanillaStats;
    }

    public static boolean getDebug() {
        return debug;
    }

    public ZstatsConfigs() {
        fc = Zstats.getPlugin(Zstats.class).getConfig();
        debug = fc.getBoolean(ZstatsConfig.DEBUG.getConfig());
        this.writeConfig();
        this.readConfig();
    }

    private synchronized void writeConfig(){
        if(Objects.requireNonNull(fc.getConfigurationSection(ZstatsConfig.VANILLA_STATS.getConfig())).getKeys(false).size() <= 1){
            System.out.println(ChatColor.YELLOW+"[Zstats] Writing config file...");
            is_writing_config = true;
            try (FileWriter fw = new FileWriter(new File(Zstats.getPlugin(Zstats.class).getDataFolder(), "config.yml"), true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                ArrayList<Statistic> stat = ZstatsGeneralUtils.getDefaultStat();
                for (Statistic s : Statistic.values()) {
                    if (s.isSubstatistic()) continue;
                    if (debug) System.out.println(s.toString());
                    if (s.toString().contains("PLAY_ONE_")) {
                        out.println("  " + s.toString() + ": true");
                    } else {
                        out.println("  " + s.toString() + (stat.contains(s) ? ": true" : ": false"));
                    }
                }
                if (debug) System.out.println("Write file done");
                fc = Zstats.getPlugin(Zstats.class).getConfig();
                notifyAll();
                is_writing_config = false;
            } catch (IOException e) {
                System.out.println(ChatColor.YELLOW+"[Zstats] An error occurred during config file write:\n" + e);
            }
        }
    }

    private synchronized void readConfig(){
        System.out.println(ChatColor.YELLOW+"[Zstats] Waiting for write config file...");
        while(is_writing_config){
            try {
                wait();
            } catch (InterruptedException e) {
                if(debug) e.printStackTrace();
            }
        }
        System.out.println(ChatColor.YELLOW+"[Zstats] Reading config file...");
        zstats = new HashMap<>();
        vanillaStats = new HashMap<>();
        booleanConfigs = new HashMap<>();
        intConfigs = new HashMap<>();
        stringConfigs = new HashMap<>();

        for (ZstatsConfig config: ZstatsConfig.getBooleanConfigs()) {
            booleanConfigs.put(config, fc.getBoolean(config.getConfig()));
        }

        for (ZstatsConfig config: ZstatsConfig.getIntConfigs()) {
            intConfigs.put(config, fc.getInt(config.getConfig()));
        }

        for (ZstatsConfig config: ZstatsConfig.getStringConfigs()) {
            stringConfigs.put(config, fc.getString(config.getConfig()));
        }

        for (String s: Objects.requireNonNull(fc.getConfigurationSection(ZstatsConfig.ZSTATS.getConfig())).getKeys(false)){
            zstats.put(s, fc.getBoolean(ZstatsConfig.ZSTATS.getConfig().concat(".").concat(s)));
        }

        for (String s: Objects.requireNonNull(fc.getConfigurationSection(ZstatsConfig.VANILLA_STATS.getConfig())).getKeys(false)){
            vanillaStats.put(s, fc.getBoolean(ZstatsConfig.VANILLA_STATS.getConfig().concat(".").concat(s)));
        }

        debug = getBooleanConfig(ZstatsConfig.DEBUG);
        System.out.println(ChatColor.YELLOW+"[Zstats] Done.");
    }
}
