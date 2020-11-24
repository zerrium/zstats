package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Zstats extends JavaPlugin{
    public static FileConfiguration fc;
    protected static Connection connection;
    public static Boolean debug, notify_discord;
    public static String notify_discord_message;
    public static ArrayList<ZPlayer> zplayer;
    public static HashMap<UUID, String> online_player;
    public static long world_size = 0L;
    public static long nether_size = 0L;
    public static long end_size = 0L;
    public static long total_size = 0L;
    public static boolean has_discordSrv, hasEssentials;

    @Override
    public void onEnable() {
        System.out.println(ChatColor.YELLOW+"[Zstats] v0.6 by zerrium");
        getServer().getPluginManager().registerEvents(new SpigotListener(), this);
        this.getCommand("zstats").setExecutor(new ZUpdater());
        System.out.println(ChatColor.YELLOW+"[Zstats] Connecting to MySQL database...");
        this.saveDefaultConfig(); //get config file
        fc = this.getConfig();
        debug = fc.getBoolean("use_debug");
        notify_discord = fc.getBoolean("notify_stats_update_to_discord");
        notify_discord_message = fc.getString("notify_message");
        //MySQL connect
        try{
            openConnection();
        } catch (SQLException | ClassNotFoundException throwables) {
            System.out.println(ChatColor.YELLOW+"[Zstats]"+ChatColor.RED+" Unable to connect to database:");
            throwables.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
        zplayer = new ArrayList<>();
        online_player = new HashMap<>();
        //database query
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("show tables");
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
            rs.close();
            final ResultSet rss = st.executeQuery("select * from player;");
            System.out.println(ChatColor.YELLOW+"[Zstats] Getting player list from database...");
            int counter = 0;
            if(!rss.next()){
                System.out.println(ChatColor.YELLOW+"[Zstats] Found nothing in database. Grabbing player lists from world save...");
                PreparedStatement ps = connection.prepareStatement("insert into player(uuid,name) values (?,?)");
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
                ps.close();
                rss.close();
            }else{
                BukkitRunnable s = new BukkitRunnable() {
                    @Override
                    public void run() {
                        AtomicInteger c = new AtomicInteger(0);
                        try{
                            do{
                                if(rss.getString("uuid").equals("000")) continue;
                                zplayer.add(new ZPlayer(UUID.fromString(rss.getString("uuid")), rss.getString("name")));
                                if(debug){
                                    System.out.println(zplayer.get(c.get()).uuid+" --- "+zplayer.get(c.get()).name);
                                }
                                c.getAndIncrement();
                            }
                            while(rss.next());
                            rss.close();
                            System.out.println(ChatColor.YELLOW+"[Zstats] Found statistic data of "+ c.get() +" players.");
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                };
                s.runTaskAsynchronously(this);
            }
        } catch (SQLException throwables) {
            System.out.println(ChatColor.YELLOW+"[Zstats]"+ChatColor.RED+" An SQL error occured:");
            throwables.printStackTrace();
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
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                new ZFilter();
            }
        };
        r.runTaskAsynchronously(this);
    }

    @Override
    public void onDisable() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        System.out.println(ChatColor.YELLOW+"[Zstats] Disabling plugin...");
    }

    //connect to MySQL database safely
    private void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + fc.getString("hostname")+ ":" + fc.getInt("port") +
                    "/" + fc.getString("database"), fc.getString("username"), fc.getString("password"));
        }
    }
}