package zerrium;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Zstats extends JavaPlugin{
    static FileConfiguration fc;
    static int version, substat_top;
    static boolean debug, notify_discord;
    static String notify_discord_message;
    static ArrayList<ZPlayer> zplayer;
    static HashMap<UUID, String> online_player;
    static long world_size, nether_size, end_size, total_size;
    static boolean has_discordSrv, hasEssentials;
    static HashMap<String, Boolean> zstats, vanilla_stats;

    private Connection connection;
    private boolean is_writing_config = false;

    @Override
    public void onEnable() {
        System.out.println(ChatColor.YELLOW+"[Zstats] v1.0 by zerrium");
        getServer().getPluginManager().registerEvents(new SpigotListener(), this);
        Objects.requireNonNull(this.getCommand("zstats")).setExecutor(new ZUpdater());
        version = getVersion();

        this.saveDefaultConfig(); //get or create config file
        fc = this.getConfig();
        debug = fc.getBoolean("use_debug");
        substat_top = fc.getInt("zstats_top");
        notify_discord = fc.getBoolean("notify_stats_update_to_discord");
        notify_discord_message = fc.getString("notify_message");

        write_config();

        System.out.println(ChatColor.YELLOW+"[Zstats] Connecting to MySQL database...");

        //MySQL connect
        try{
            connection = SqlCon.openConnection();
        } catch (SQLException throwables) {
            System.out.println(ChatColor.YELLOW+"[Zstats]"+ChatColor.RED+" Unable to connect to database:");
            throwables.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
        zplayer = new ArrayList<>();
        online_player = new HashMap<>();

        //database query
        Statement st = null;
        ResultSet rs = null;
        ResultSet rss = null;
        PreparedStatement ps = null;
        try {
            st = connection.createStatement();
            rs = st.executeQuery("show tables");
            if(!rs.next()){
                st.executeUpdate("create table player(" +
                        "    uuid varchar(50) not null," +
                        "    name text not null," +
                        "    primary key(uuid));");
                st.executeUpdate("create table stats(" +
                        "    uuid varchar(50) not null," +
                        "    stat text not null," +
                        "    val bigint(19) not null," +
                        "    foreign key(uuid) references player(uuid));");
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
                        System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Found player with uuid of " + uuid.toString() + " associates with " + name);
                        zplayer.add(new ZPlayer(uuid, (name == null ? "null" : name)));
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
                        zplayer.add(new ZPlayer(UUID.fromString(rss.getString("uuid")), rss.getString("name")));
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

        if(Bukkit.getPluginManager().getPlugin("DiscordSRV") != null || Bukkit.getPluginManager().getPlugin("discordsrv") != null){
            System.out.println(ChatColor.YELLOW+"[Zstats] DiscordSRV plugin detected. Messaging system to DiscordSRV is hooked.");
            has_discordSrv = true;
        }else{
            System.out.println(ChatColor.YELLOW+"[Zstats] No DiscordSRV plugin detected. Disabled messaging system to DiscordSRV. ");
            has_discordSrv = false;
        }
        if(Bukkit.getPluginManager().getPlugin("Essentials") != null || Bukkit.getPluginManager().getPlugin("EssentialsX") != null){
            System.out.println(ChatColor.YELLOW+"[Zstats] Essentials plugin detected. AFK detection for AFK time stats enabled.");
            hasEssentials = true;
        }else{
            System.out.println(ChatColor.YELLOW+"[Zstats] No Essentials plugin detected. Disabled AFK time stats");
            hasEssentials = false;
        }
        ZFilter.begin();
        read_config();
    }

    @Override
    public void onDisable() {
        SqlCon.closeConnection();
        System.out.println(ChatColor.YELLOW+"[Zstats] Disabling plugin...");
    }

    private synchronized void write_config(){
        if(fc.getString("vanilla_stats.MOB_KILLS") == null){
            System.out.println(ChatColor.YELLOW+"[Zstats] Writing config file...");
            is_writing_config = true;
            try (FileWriter fw = new FileWriter(new File(getDataFolder(), "config.yml"), true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                ArrayList<Statistic> stat = getDefaultStat();
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
                fc = this.getConfig();
                notifyAll();
                is_writing_config = false;
            } catch (IOException e) {
                System.out.println(ChatColor.YELLOW+"[Zstats] An error occurred during config file write:\n" + e);
            }
        }
    }

    private synchronized void read_config(){
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
        vanilla_stats = new HashMap<>();
        for (String s: Objects.requireNonNull(fc.getConfigurationSection("zstats")).getKeys(false)){
            zstats.put(s, fc.getBoolean("zstats."+s));
        }
        for (String s: Objects.requireNonNull(fc.getConfigurationSection("vanilla_stats")).getKeys(false)){
            vanilla_stats.put(s, fc.getBoolean("vanilla_stats."+s));
        }
        System.out.println(ChatColor.YELLOW+"[Zstats] Done.");
    }

    protected static void updateWorldSize(){
        end_size = 0L;
        nether_size = 0L;
        world_size = 0L;
        total_size = 0L;

        Bukkit.getWorlds().forEach(i ->{
            switch(i.getEnvironment()){
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

    protected static int getVersion(){
        String v = Bukkit.getServer().getVersion();
        if (v.contains("1.8")){
            if (v.contains("1.8.1")) return 0;
            else if (v.contains("1.8.2")) return 0;

            //CHEST_OPENED and ITEM_ENCHANTED stat
            else if (v.contains("1.8.3")) return 1;
            else if (v.contains("1.8.4")) return 1;
            else if (v.contains("1.8.5")) return 1;
            else if (v.contains("1.8.6")) return 1;
            else if (v.contains("1.8.7")) return 1;
            else if (v.contains("1.8.8")) return 1;
            else if (v.contains("1.8.9")) return 1;

            else return 0;
        }
        //AVIATE_ONE_CM (elytra distance) stat, SLEEP_IN_BED stat, SHIELD stat
        else if (v.contains("1.9")) return 2;
        else if (v.contains("1.10")) return 2;
        else if (v.contains("1.11")) return 2;
        else if (v.contains("1.12")) return 2;

        //Trident stat, PLAY_ONE_MINUTE on 1.13+ instead of PLAY_ONE_TICK on <1.13 only the name changes, it still records the tick actually
        else if (v.contains("1.13")) return 3;

        //Crossbow stat
        else if (v.contains("1.14")) return 4;

        //Supports OfflinePlayer#getStatistic
        else if (v.contains("1.15")) return 5;
        else if (v.contains("1.16")) return 5;

        else{
            if(Zstats.debug) System.out.println(ChatColor.YELLOW+"[Zstats] Unsupported version: " + v +" Set to ZVersion 5");
            return 5;
        }
    }

    private ArrayList<Statistic> getDefaultStat(){
        ArrayList<Statistic> x= new ArrayList<>();

        if(version > 1){
            x.add(Statistic.AVIATE_ONE_CM); //1.9+
            x.add(Statistic.SLEEP_IN_BED); //1.9+
        }

        x.add(Statistic.DAMAGE_DEALT);
        x.add(Statistic.DAMAGE_TAKEN);
        x.add(Statistic.MOB_KILLS);
        x.add(Statistic.DEATHS);
        x.add(Statistic.SPRINT_ONE_CM); //1.8+
        x.add(Statistic.WALK_ONE_CM);
        x.add(Statistic.CROUCH_ONE_CM); //1.8+
        x.add(Statistic.BOAT_ONE_CM);
        x.add(Statistic.TRADED_WITH_VILLAGER); //1.8+
        x.add(Statistic.TALKED_TO_VILLAGER); //1.8+
        x.add(Statistic.CHEST_OPENED); //1.8.3+
        x.add(Statistic.FISH_CAUGHT);
        x.add(Statistic.ITEM_ENCHANTED); //1.8.3+

        return x;
    }
}