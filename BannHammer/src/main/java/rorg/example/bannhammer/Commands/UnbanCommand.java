package rorg.example.bannhammer.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import rorg.example.bannhammer.PunishmentGUI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UnbanCommand implements CommandExecutor {

    private final Connection connection;
    private final PunishmentGUI plugin;

    public UnbanCommand(Connection connection, PunishmentGUI plugin) {
        this.connection = connection;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unban <player>");
            return false;
        }

        String playerNameOrUUID = args[0];
        UUID targetUUID = null;


        try {
            targetUUID = UUID.fromString(playerNameOrUUID);
        } catch (IllegalArgumentException e) {

            targetUUID = getUUIDFromName(playerNameOrUUID);
        }

        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM bans WHERE player_uuid = ?");
            statement.setString(1, targetUUID.toString());
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                sender.sendMessage(ChatColor.GREEN + "Player " + playerNameOrUUID + " has been unbanned.");
            } else {
                sender.sendMessage(ChatColor.RED + "Player " + playerNameOrUUID + " is not banned.");
            }
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.RED + "Failed to unban player.");
            plugin.getLogger().severe("Failed to unban player: " + playerNameOrUUID);
            e.printStackTrace();
        }

        return true;
    }

    private UUID getUUIDFromName(String playerName) {
        UUID uuid = null;
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT player_uuid FROM bans WHERE player_name = ? COLLATE utf8mb4_general_ci"
            );
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String uuidString = resultSet.getString("player_uuid");
                if (uuidString != null) {
                    uuid = UUID.fromString(uuidString);
                    plugin.getLogger().info("Found UUID for player " + playerName + ": " + uuid.toString());
                } else {
                    plugin.getLogger().info("No UUID found for player " + playerName);
                }
            } else {
                plugin.getLogger().info("Player name not found in database: " + playerName);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL error while fetching UUID for player: " + playerName);
            e.printStackTrace();
        }
        return uuid;
    }
}
