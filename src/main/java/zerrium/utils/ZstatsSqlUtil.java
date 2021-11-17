package zerrium.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import zerrium.configs.ZstatsConfigs;
import zerrium.models.ZstatsConfig;

import java.sql.Connection;
import java.sql.SQLException;

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
}
