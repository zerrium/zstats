package zerrium;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Zstats extends JavaPlugin{
    static FileConfiguration fc;
    private Connection connection;
    static Boolean debug, notify_discord;
    static String notify_discord_message;
    static ArrayList<ZPlayer> zplayer;
    static HashMap<UUID, String> online_player;
    static long world_size = 0L;
    static long nether_size = 0L;
    static long end_size = 0L;
    static long total_size = 0L;
    static boolean has_discordSrv, hasEssentials;

    @Override
    public void onEnable() {
        System.out.println(ChatColor.YELLOW+"[Zstats] v0.8.2 (1.12.2 Edition) by zerrium");
        getServer().getPluginManager().registerEvents(new SpigotListener(), this);
        Objects.requireNonNull(this.getCommand("zstats")).setExecutor(new ZUpdater());
        System.out.println(ChatColor.YELLOW+"[Zstats] Connecting to MySQL database...");
        this.saveDefaultConfig(); //get config file
        fc = this.getConfig();
        debug = fc.getBoolean("use_debug");
        notify_discord = fc.getBoolean("notify_stats_update_to_discord");
        notify_discord_message = fc.getString("notify_message");
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
            rss = st.executeQuery("select * from player;");
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
                        zplayer.add(new ZPlayer(uuid, name));
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
            System.out.println(ChatColor.YELLOW+"[Zstats]"+ChatColor.RED+" An SQL error occured:");
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
            System.out.println(ChatColor.YELLOW+"[Zstats] No Essentials plugin detected. Disabled AFK detection for sleep notification");
            hasEssentials = false;
        }
        ZFilter.begin();
    }

    @Override
    public void onDisable() {
        SqlCon.closeConnection();
        System.out.println(ChatColor.YELLOW+"[Zstats] Disabling plugin...");
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
}