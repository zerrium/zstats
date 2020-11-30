package zerrium;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlCon {
    private final static String hostname = Zstats.fc.getString("hostname");
    private final static int port = Zstats.fc.getInt("port");
    private final static String db_name = Zstats.fc.getString("database");
    private final static String username = Zstats.fc.getString("username");
    private final static String password = Zstats.fc.getString("password");
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
        ds = new HikariDataSource( config );
    }

    private SqlCon() {}

    protected static Connection openConnection() throws SQLException {
        return ds.getConnection();
    }
}
