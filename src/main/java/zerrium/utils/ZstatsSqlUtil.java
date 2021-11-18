package zerrium.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import zerrium.configs.ZstatsConfigs;
import zerrium.models.ZstatsConfig;
import zerrium.models.ZstatsPlayer;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

public class ZstatsSqlUtil {
    private final static String hostname = ZstatsConfigs.getStringConfig(ZstatsConfig.DB_HOST);
    private final static int port = ZstatsConfigs.getIntConfig(ZstatsConfig.DB_PORT);
    private final static String db_name = ZstatsConfigs.getStringConfig(ZstatsConfig.DB_NAME);
    private final static String username = ZstatsConfigs.getStringConfig(ZstatsConfig.DB_USER);
    private final static String password = ZstatsConfigs.getStringConfig(ZstatsConfig.DB_PASSWORD);
    private final static boolean useSSL = ZstatsConfigs.getBooleanConfig(ZstatsConfig.DB_SSL);
    private final static HikariConfig config = new HikariConfig();
    private final static HikariDataSource ds;

    static {
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl( "jdbc:mysql://" + hostname + ":" + port + "/" + db_name );
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        config.addDataSourceProperty("useSSL", useSSL);
        config.addDataSourceProperty("maxLifetime", 18000);
        ds = new HikariDataSource( config );
    }

    public static Connection openConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void closeConnection(){
        ds.close();
    }

    public static String getPlayerTableName(String query) {
        return query.replace("<player>", "");
    }

    static void initializeSQL(Connection connection, ArrayList<ZstatsPlayer> zplayer){
        final boolean debug = ZstatsConfigs.getDebug();

        Statement st = null;
        ResultSet rs = null;
        ResultSet rss = null;
        PreparedStatement ps = null;
        try {
            st = connection.createStatement();
            rs = st.executeQuery("show tables");
            if(!rs.next()){
                st.executeUpdate("    create table player(" +
                        "    uuid varchar(50) not null," +
                        "    name text not null," +
                        "    primary key(uuid));");
                st.executeUpdate("    create table stats(" +
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
