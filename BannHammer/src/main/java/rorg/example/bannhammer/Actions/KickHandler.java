package rorg.example.bannhammer.Actions;

import org.bukkit.entity.Player;
import rorg.example.bannhammer.PunishmentGUI;

import java.sql.Connection;
import java.util.UUID;

public class KickHandler {
    private final Connection connection;
    private final PunishmentGUI plugin;

    public KickHandler(Connection connection, PunishmentGUI plugin) {
        this.connection = connection;
        this.plugin = plugin;
    }

    public void handleKick(Player player, UUID targetUUID, String reason) {
        player.sendMessage("Player kicked for reason: " + reason);
    }
}
