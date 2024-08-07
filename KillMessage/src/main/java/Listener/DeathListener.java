package Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

public class DeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            victim.sendMessage("§9§lSkyPvP §8§l» §7You got killed by §9" + killer.getName() + "§7.");
            killer.sendMessage("§9§lSkyPvP §8§l» §7You just killed §9" + victim.getName() + "§7.");
        }
    }
}
