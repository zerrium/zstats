package zerrium.utils;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import zerrium.Zstats;
import zerrium.models.ZstatsPlayer;
import zerrium.configs.ZstatsConfigs;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ZstatsGeneralUtils {
    private static ArrayList<ZstatsPlayer> zplayer;
    private static HashMap<UUID, String> onlinePlayer;

    public static long world_size, nether_size, end_size, total_size;

    public static void init(Connection connection) {
        zplayer = new ArrayList<>();
        onlinePlayer = new HashMap<>();
        //database query
        ZstatsSqlUtil.initializeSQL(connection, zplayer);
    }

    public static ArrayList<ZstatsPlayer> getZplayer() {
        return zplayer;
    }

    public static HashMap<UUID, String> getOnlinePlayer() {
        return onlinePlayer;
    }

    public static ArrayList<Statistic> getDefaultStat(){
        final int version = Zstats.getVersion();
        ArrayList<Statistic> x= new ArrayList<>(List.of(
                Statistic.DAMAGE_DEALT,
                Statistic.DAMAGE_TAKEN,
                Statistic.MOB_KILLS,
                Statistic.DEATHS,
                Statistic.SPRINT_ONE_CM, //1.8+
                Statistic.WALK_ONE_CM,
                Statistic.CROUCH_ONE_CM, //1.8+
                Statistic.BOAT_ONE_CM,
                Statistic.TRADED_WITH_VILLAGER, //1.8+
                Statistic.TALKED_TO_VILLAGER, //1.8+
                Statistic.FISH_CAUGHT
        ));

        if(version > 0) {
            x.add(Statistic.CHEST_OPENED); //1.8.3+
            x.add(Statistic.ITEM_ENCHANTED); //1.8.3+
        }

        if(version > 1){
            x.add(Statistic.AVIATE_ONE_CM); //1.9+
            x.add(Statistic.SLEEP_IN_BED); //1.9+
        }

        return x;
    }

    public static void updateWorldSize(){
        final boolean debug = ZstatsConfigs.getDebug();

        end_size = 0L;
        nether_size = 0L;
        world_size = 0L;
        total_size = 0L;

        Bukkit.getWorlds().forEach(i ->{
            switch (i.getEnvironment()) {
                case NORMAL:
                    world_size = FileUtils.sizeOfDirectory(i.getWorldFolder());
                    total_size += world_size;
                    break;

                case NETHER:
                    nether_size = FileUtils.sizeOfDirectory(i.getWorldFolder());
                    total_size += nether_size;
                    break;

                case THE_END:
                    end_size = FileUtils.sizeOfDirectory(i.getWorldFolder());
                    total_size += end_size;
                    break;

                default:
                    total_size += total_size;
                    break;
            }
            if(debug) System.out.println("Got world size of "+i.getName());
        });
        if(debug) System.out.println("Total size "+total_size);
    }
}
