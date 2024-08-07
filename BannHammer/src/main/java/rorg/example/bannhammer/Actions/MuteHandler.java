package rorg.example.bannhammer.Actions;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class MuteHandler {
    private final Connection connection;
    private final JavaPlugin plugin;

    public MuteHandler(Connection connection, JavaPlugin plugin) {
        this.connection = connection;
        this.plugin = plugin;
    }

    public void handleMute(Player player, UUID targetUUID, String reason, int duration) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO mutes (player_uuid, reason, muted_by, muted_until) VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE)) " +
                            "ON DUPLICATE KEY UPDATE reason = VALUES(reason), muted_by = VALUES(muted_by), muted_until = VALUES(muted_until)"
            );
            statement.setString(1, targetUUID.toString());
            statement.setString(2, reason);
            statement.setString(3, player.getName());
            statement.setInt(4, duration);
            statement.executeUpdate();

            player.sendMessage(ChatColor.YELLOW + "Player muted for " + duration + " minutes.");
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "Failed to mute player.");
            plugin.getLogger().log(Level.SEVERE, "Error muting player " + targetUUID, e);
        }
    }
}
