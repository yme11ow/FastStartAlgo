package org.github.yme11ow.faststartalgo;
import org.bukkit.plugin.java.JavaPlugin;
import org.github.yme11ow.faststartalgo.commands.VillageLocator;

public class FastStartAlgo extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("FastStartAlgo enabled!");
        this.getCommand("villagelocator").setExecutor(new VillageLocator());
    }

    @Override
    public void onDisable() {
        getLogger().info("FastStartAlgo disabled!");
    }
}