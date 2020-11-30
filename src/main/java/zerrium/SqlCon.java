package zerrium;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlCon {
    private final static String hostname = Zstats.fc.getString("hostname");
    private final static int port = Zstats.fc.getInt("port");
    private final static String db_name = Zstats.fc.getString("database");
    private final static String username = Zstats.fc.getString("username");
    private final static String password = Zstats.fc.getString("password");

    //connect to MySQL database safely
    protected Connection openConnection() throws SQLException, ClassNotFoundException {
        synchronized (this) {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + db_name, username, password);
        }
    }
}
