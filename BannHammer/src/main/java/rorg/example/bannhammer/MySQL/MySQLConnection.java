package rorg.example.bannhammer.MySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

public class MySQLConnection {
    private final JavaPlugin plugin;
    private Connection connection;

    public MySQLConnection(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            String url = "jdbc:mysql://localhost:3306/punishment_system?characterEncoding=UTF-8";
            String username = "LADENACH";
            String password = "DRÜCKAB";
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("Connected to the database successfully.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to the database.", e);
        }
    }

    public void createTables() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS bans (" +
                "player_uuid CHAR(36) NOT NULL," +
                "reason VARCHAR(255) NOT NULL," +
                "banned_by VARCHAR(255) NOT NULL," +
                "banned_until TIMESTAMP NULL," +
                "PRIMARY KEY (player_uuid)" +
                ")");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS mutes (" +
                "player_uuid CHAR(36) NOT NULL," +
                "reason VARCHAR(255) NOT NULL," +
                "muted_by VARCHAR(255) NOT NULL," +
                "muted_until TIMESTAMP NULL," +
                "PRIMARY KEY (player_uuid)" +
                ")");
        statement.close();
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close the database connection.", e);
            }
        }
    }
}
