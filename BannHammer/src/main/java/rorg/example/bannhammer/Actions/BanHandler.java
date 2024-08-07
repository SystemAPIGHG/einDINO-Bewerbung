package rorg.example.bannhammer.Actions;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class BanHandler {
    private final Connection connection;
    private final JavaPlugin plugin;

    public BanHandler(Connection connection, JavaPlugin plugin) {
        this.connection = connection;
        this.plugin = plugin;
    }

    public void handleBan(Player player, UUID targetUUID, String reason, int days) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO bans (player_uuid, reason, banned_by, banned_until) VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL ? DAY)) " +
                            "ON DUPLICATE KEY UPDATE reason = VALUES(reason), banned_by = VALUES(banned_by), banned_until = VALUES(banned_until)"
            );
            statement.setString(1, targetUUID.toString());
            statement.setString(2, reason);
            statement.setString(3, player.getName());
            statement.setInt(4, days);
            statement.executeUpdate();

            Player targetPlayer = player.getServer().getPlayer(targetUUID);
            if (targetPlayer != null && targetPlayer.isOnline()) {
                String banMessage = ChatColor.RED + "FinalMC" + ChatColor.DARK_GRAY + " » " + ChatColor.GRAY + "Permanently banned\n\n"
                        + ChatColor.RED + "Reason" + ChatColor.DARK_GRAY + " » " + ChatColor.GRAY + reason + "\n\n"
                        + ChatColor.DARK_GRAY + "Unban application in Discord or Forum\n"
                        + ChatColor.GOLD + "Discord" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "discord.gg/finalmc\n"
                        + ChatColor.GOLD + "Forum" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "example.com";
                targetPlayer.kickPlayer(banMessage);
            }

            player.sendMessage(ChatColor.GREEN + "Player banned successfully.");
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "Failed to ban player.");
            plugin.getLogger().log(Level.SEVERE, "Error banning player " + targetUUID, e);
        }
    }
}
