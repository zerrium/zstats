package zerrium.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import zerrium.Zstats;
import zerrium.configs.ZstatsConfigs;
import zerrium.models.ZstatsConfig;
import zerrium.models.ZstatsPlayer;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public class ZstatsSqlUtil {
    private final static String hostname = ZstatsConfigs.getStringConfig(ZstatsConfig.DB_HOST);
    private final static int port = ZstatsConfigs.getIntConfig(ZstatsConfig.DB_PORT);
    private final static String db_name = ZstatsConfigs.getStringConfig(ZstatsConfig.DB_NAME);
    private final static String username = ZstatsConfigs.getStringConfig(ZstatsConfig.DB_USER);
    private final static String password = ZstatsConfigs.getStringConfig(ZstatsConfig.DB_PASSWORD);
    private final static boolean useSSL = ZstatsConfigs.getBooleanConfig(ZstatsConfig.DB_SSL);
    private final static HikariConfig config = new HikariConfig();
    private final static HikariDataSource ds;

    private final static Logger log = Zstats.getPlugin(Zstats.class).getLogger();

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

    public static void closeConnection() {
        ds.close();
    }

    public static String getTableName(String query) {
        String tablePlayer = "player";
        String tableStats = "stats";
        String prefix = ZstatsConfigs.getStringConfig(ZstatsConfig.DB_TABLE_PREFIX);
        String suffix = ZstatsConfigs.getStringConfig(ZstatsConfig.DB_TABLE_SUFFIX);

        if(!prefix.isBlank()) {
            tablePlayer = prefix.concat("_").concat(tablePlayer);
            tableStats = prefix.concat("_").concat(tableStats);
        }

        if(!suffix.isBlank()) {
            tablePlayer = tablePlayer.concat("_").concat(suffix);
            tableStats = tableStats.concat("_").concat(suffix);
        }

        return query.replace("<$zplayer>", tablePlayer)
                .replace("<$zstats>", tableStats);
    }

    static boolean validateTableNameConfig() {
        final String regex = "^(?i)[a-z_][a-z0-9_]*?$";
        final String prefix = ZstatsConfigs.getStringConfig(ZstatsConfig.DB_TABLE_PREFIX);
        final String suffix = ZstatsConfigs.getStringConfig(ZstatsConfig.DB_TABLE_SUFFIX);

        return (prefix.isEmpty() || prefix.matches(regex)) && (suffix.isEmpty() || suffix.matches(regex));
    }

    static void initializeSQL(Connection connection, ArrayList<ZstatsPlayer> zplayer){
        Statement st = null;
        ResultSet rs = null;
        ResultSet rss = null;
        PreparedStatement ps = null;
        PreparedStatement pss = null;
        try {
            pss = connection.prepareStatement("select table_name from information_schema.tables" +
                    "    where table_schema=?" +
                    "    and table_name=?" +
                    "    or table_name=?;");
            pss.setString(1, ZstatsConfigs.getStringConfig(ZstatsConfig.DB_NAME));
            pss.setString(2, getTableName("<$zplayer>"));
            pss.setString(3, getTableName("<$zstats>"));
            st = connection.createStatement();
            rs = pss.executeQuery();
            if(!rs.next()){
                st.executeUpdate(getTableName("create table <$zplayer>(" +
                        "    uuid varchar(50) not null," +
                        "    name text not null," +
                        "    primary key(uuid));"));
                st.executeUpdate(getTableName("create table <$zstats>(" +
                        "    uuid varchar(50) not null," +
                        "    stat text not null," +
                        "    val bigint(19) not null," +
                        "    foreign key(uuid) references <$zplayer>(uuid));"));
            }
            rss = st.executeQuery(getTableName("select * from <$zplayer> where uuid != \"000\";"));
            log.info(ChatColor.YELLOW+"[Zstats]"+ChatColor.RESET+" Getting player list from database...");
            int counter = 0;
            if(!rss.next()){
                log.info(ChatColor.YELLOW+"[Zstats]"+ChatColor.RESET+" Found nothing in database. Grabbing player lists from world save...");
                ps = connection.prepareStatement(getTableName("insert into <$zplayer>(uuid,name) values (?,?)"));
                for(OfflinePlayer i: Bukkit.getOfflinePlayers()){
                    if(i.hasPlayedBefore()) {
                        counter++;
                        UUID uuid = i.getUniqueId();
                        String name = i.getName();
                        if(name == null){
                            log.warning(ChatColor.YELLOW+"[Zstats] Warning! Found a player with uuid of " + uuid.toString() + " has null display name. Skipped this player.");
                            log.warning(ChatColor.YELLOW+"[Zstats] Suggestion: you need to check your online_mode option in your server.properties and check if you have mixed online and offline players in your world save.");
                            continue;
                        }
                        log.info(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Found player with uuid of " + uuid.toString() + " associates with " + name);
                        zplayer.add(new ZstatsPlayer(uuid, name));
                        ps.setString(1, uuid.toString());
                        ps.setString(2, name);
                        ps.executeUpdate();
                    }
                }
                ps.setString(1, "000");
                ps.setString(2, "Server");
                ps.executeUpdate();
                log.info(ChatColor.YELLOW+"[Zstats]"+ChatColor.RESET+" Found statistic data of "+ counter +" players.");
            }else{
                int c = 0;
                do{
                    if(rss.getString("uuid").equals("000")) continue;
                    zplayer.add(new ZstatsPlayer(UUID.fromString(rss.getString("uuid")), rss.getString("name")));
                    log.fine("[Zstats: "+ZstatsSqlUtil.class.toString()+"] "+zplayer.get(c).uuid+" --- "+zplayer.get(c).name);
                    c++;
                }
                while(rss.next());
                log.info(ChatColor.YELLOW+"[Zstats]"+ChatColor.RESET+" Found statistic data of "+ c +" players.");
            }
        } catch (SQLException throwables) {
            log.severe(ChatColor.YELLOW+"[Zstats]"+ChatColor.RED+" An SQL error occured:\n");
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

                assert pss != null;
                pss.close();

                connection.close();
            } catch (Exception e) {
                log.fine("[Zstats: "+ZstatsSqlUtil.class.toString()+"] "+ e );
            }
        }
    }
}
