package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class SpigotEvent extends JavaPlugin{
    public static FileConfiguration fc;
    protected static Connection connection;
    public static Boolean debug;

    @Override
    public void onEnable() {
        System.out.println(ChatColor.YELLOW+"[Stat2Discord] v0.1 by zerrium");
        getServer().getPluginManager().registerEvents(new SpigotListener(), this);
        System.out.println(ChatColor.YELLOW+"[Stat2Discord] Connecting to MySQL database...");
        this.saveDefaultConfig(); //get config file
        fc = this.getConfig();
        debug = fc.getBoolean("use_debug");
        //MySQL connect
        try{
            openConnection();
        } catch (SQLException | ClassNotFoundException throwables) {
            System.out.println(ChatColor.YELLOW+"[Stat2Discord]"+ChatColor.RED+" Unable to connect to database:");
            throwables.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
        Discord.zplayer = new ArrayList<>();
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
            rs = st.executeQuery("select * from player;");
            System.out.println(ChatColor.YELLOW+"[Stat2Discord] Getting player list from database...");
            int counter = 0;
            if(!rs.next()){
                System.out.println(ChatColor.YELLOW+"[Stat2Discord] Found nothing in database. Grabbing player lists from world save...");
                PreparedStatement ps = connection.prepareStatement("insert into player(uuid,name) values (?,?)");
                for(OfflinePlayer i: Bukkit.getOfflinePlayers()){
                    if(i.hasPlayedBefore()) {
                        counter++;
                        UUID uuid = i.getUniqueId();
                        String name = i.getName();
                        System.out.println(ChatColor.YELLOW + "[Stat2Discord]" + ChatColor.RESET + " Found player with uuid of " + uuid.toString() + " associates with " + name);
                        Discord.zplayer.add(new ZPlayer(uuid, name));
                        ps.setString(1, uuid.toString());
                        ps.setString(2, name);
                        ps.executeUpdate();
                    }
                }
                System.out.println(ChatColor.YELLOW+"[Stat2Discord] Found statistic data of "+ counter +" players.");
                ps.close();
            }else{
                ResultSet finalRs = rs;
                BukkitRunnable s = new BukkitRunnable() {
                    @Override
                    public void run() {
                        AtomicInteger c = new AtomicInteger(0);
                        try{
                            do{
                                Discord.zplayer.add(new ZPlayer(UUID.fromString(finalRs.getString("uuid")), finalRs.getString("name")));
                                if(debug){
                                    System.out.println(Discord.zplayer.get(c.get()).uuid+" --- "+Discord.zplayer.get(c.get()).name);
                                }
                                c.getAndIncrement();
                            }
                            while(finalRs.next());
                            finalRs.close();
                            System.out.println(ChatColor.YELLOW+"[Stat2Discord] Found statistic data of "+ c.get() +" players.");
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                };
                s.runTaskAsynchronously(this);
            }
            rs.close();
        } catch (SQLException throwables) {
            System.out.println(ChatColor.YELLOW+"[Stat2Discord]"+ChatColor.RED+" An SQL error occured:");
            throwables.printStackTrace();
        }

        System.out.println(ChatColor.YELLOW+"[Stat2Discord] Enabling Discord asynchronously...");
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                new ZFilter();
                new Discord();
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
        System.out.println(ChatColor.YELLOW+"[Stat2Discord] v0.1 disabling plugin");
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