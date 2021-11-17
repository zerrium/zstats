package zerrium.utils;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
        initializeSQL(connection);
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
                case NORMAL -> {
                    world_size = FileUtils.sizeOfDirectory(i.getWorldFolder());
                    total_size += world_size;
                }
                case NETHER -> {
                    nether_size = FileUtils.sizeOfDirectory(i.getWorldFolder());
                    total_size += nether_size;
                }
                case THE_END -> {
                    end_size = FileUtils.sizeOfDirectory(i.getWorldFolder());
                    total_size += end_size;
                }
                default -> total_size += total_size;
            }
            if(debug) System.out.println("Got world size of "+i.getName());
        });
        if(debug) System.out.println("Total size "+total_size);
    }

    private static void initializeSQL(Connection connection){
        final boolean debug = ZstatsConfigs.getDebug();

        Statement st = null;
        ResultSet rs = null;
        ResultSet rss = null;
        PreparedStatement ps = null;
        try {
            st = connection.createStatement();
            rs = st.executeQuery("show tables");
            if(!rs.next()){
                st.executeUpdate("""
                            create table player(
                            uuid varchar(50) not null,
                            name text not null,
                            primary key(uuid));
                        """);
                st.executeUpdate("""
                            create table stats(
                            uuid varchar(50) not null,
                            stat text not null,
                            val bigint(19) not null,
                            foreign key(uuid) references player(uuid));
                        """);
            }
            rss = st.executeQuery("select * from player where uuid != \"000\";");
            System.out.println(ChatColor.YELLOW+"[Zstats] Getting player list from database...");
            int counter = 0;
            if(!rss.next()){
                System.out.println(ChatColor.YELLOW+"[Zstats] Found nothing in database. Grabbing player lists from world save...");
                ps = connection.prepareStatement("insert into player(uuid,name) values (?,?)");
                for(OfflinePlayer i: Bukkit.getOfflinePlayers()){
                    if(i.hasPlayedBefore()) {
                        counter++;
                        UUID uuid = i.getUniqueId();
                        String name = i.getName();
                        if(name == null){
                            System.out.println(ChatColor.YELLOW+"[Zstats] Warning! Found a player with uuid of " + uuid.toString() + " has null display name. Skipped this player.");
                            System.out.println(ChatColor.YELLOW+"[Zstats] Suggestion: you need to check your online_mode option in your server.properties and check if you have mixed online and offline players in your world save.");
                            continue;
                        }
                        System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Found player with uuid of " + uuid.toString() + " associates with " + name);
                        zplayer.add(new ZstatsPlayer(uuid, name));
                        ps.setString(1, uuid.toString());
                        ps.setString(2, name);
                        ps.executeUpdate();
                    }
                }
                ps.setString(1, "000");
                ps.setString(2, "Server");
                ps.executeUpdate();
                System.out.println(ChatColor.YELLOW+"[Zstats] Found statistic data of "+ counter +" players.");
            }else{
                int c = 0;
                do{
                    if(rss.getString("uuid").equals("000")) continue;
                    zplayer.add(new ZstatsPlayer(UUID.fromString(rss.getString("uuid")), rss.getString("name")));
                    if(debug){
                        System.out.println(zplayer.get(c).uuid+" --- "+zplayer.get(c).name);
                    }
                    c++;
                }
                while(rss.next());
                System.out.println(ChatColor.YELLOW+"[Zstats] Found statistic data of "+ c +" players.");
            }
        } catch (SQLException throwables) {
            System.out.println(ChatColor.YELLOW+"[Zstats]"+ChatColor.RED+" An SQL error occured:\n");
            throwables.printStackTrace();
        } finally {
            try {
                assert st != null;
                st.close();

                assert rs != null;
                rs.close();

                assert rss != null;
                rss.close();

                assert ps != null;
                ps.close();

                connection.close();
            } catch (Exception e) {
                if(debug) System.out.println("[Zstats] "+ e );
            }
        }
    }
}
