package rorg.example.bannhammer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import rorg.example.bannhammer.Commands.UnbanCommand;
import rorg.example.bannhammer.MySQL.MySQLConnection;
import rorg.example.bannhammer.Actions.*;


import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class PunishmentGUI extends JavaPlugin implements Listener {
    private MySQLConnection mySQLConnection;
    private BanHandler banHandler;
    private MuteHandler muteHandler;
    private KickHandler kickHandler;

    @Override
    public void onEnable() {
        mySQLConnection = new MySQLConnection(this);
        mySQLConnection.connect();

        try {
            mySQLConnection.createTables();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to create database tables.", e);
        }

        banHandler = new BanHandler(mySQLConnection.getConnection(), this);
        muteHandler = new MuteHandler(mySQLConnection.getConnection(), this);
        kickHandler = new KickHandler(mySQLConnection.getConnection(), this);

        getCommand("punish").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                openPunishGUI(player);
            }
            return true;
        });
        getCommand("unban").setExecutor(new UnbanCommand(mySQLConnection.getConnection(), this));
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        if (mySQLConnection != null) {
            mySQLConnection.close();
        }
    }

    private void openPunishGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, ChatColor.RED + "Select Punishment");

        inv.setItem(0, createItem(Material.IRON_SWORD, ChatColor.RED + "Ban"));
        inv.setItem(1, createItem(Material.PAPER, ChatColor.YELLOW + "Mute"));
        inv.setItem(2, createItem(Material.COMMAND, ChatColor.GREEN + "Kick"));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.RED + "Select Punishment")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() != null) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem.getType() == Material.IRON_SWORD) {
                    banHandler.handleBan(player, player.getUniqueId(), "Ban reason", 1);
                } else if (clickedItem.getType() == Material.PAPER) {
                    muteHandler.handleMute(player, player.getUniqueId(), "Mute reason", 1);
                } else if (clickedItem.getType() == Material.COMMAND) {
                    kickHandler.handleKick(player, player.getUniqueId(), "Kick reason");
                }
            }
        }
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID playerUUID = event.getUniqueId();
        String playerName = event.getName();

        if (playerUUID != null) {
            try (PreparedStatement statement = mySQLConnection.getConnection().prepareStatement("SELECT banned_until FROM bans WHERE player_uuid = ?")) {
                statement.setString(1, playerUUID.toString());
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    Timestamp bannedUntil = resultSet.getTimestamp("banned_until");
                    if (bannedUntil != null && bannedUntil.after(new Timestamp(System.currentTimeMillis()))) {
                        String banMessage = ChatColor.RED + "FinalMC" + ChatColor.DARK_GRAY + " » " + ChatColor.GRAY + "Permanently banned\n\n"
                                + ChatColor.RED + "Reason" + ChatColor.DARK_GRAY + " » " + ChatColor.GRAY + "reason\n\n"
                                + ChatColor.DARK_GRAY + "Unban application in Discord or Forum\n"
                                + ChatColor.GOLD + "Discord" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "discord.gg/finalmc\n"
                                + ChatColor.GOLD + "Forum" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "example.com";

                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, banMessage);
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Error checking ban status for player " + playerName, e);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Error checking ban status. Please try again later.");
            }
        }
    }
}
