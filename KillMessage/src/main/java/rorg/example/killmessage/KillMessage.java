package rorg.example.killmessage;

import Listener.DeathListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class KillMessage extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new DeathListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
