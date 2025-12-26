package me.thetwixhunter.lifesteal;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.List;

public class PlayerJoinListener implements Listener {

    private final LifeSteal plugin;

    public PlayerJoinListener(LifeSteal plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        syncPlayerHearts(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        syncPlayerHearts(event.getPlayer());
    }

    private void syncPlayerHearts(Player player) {
        // Only sync hearts if player is in an enabled world
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        String worldName = player.getWorld().getName();
        
        if (!enabledWorlds.contains(worldName)) {
            plugin.getLogger().fine("[DEBUG] Not syncing hearts for " + player.getName() + " - not in enabled world");
            return;
        }
        
        // Load hearts from file and apply to player
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            double storedHearts = plugin.getHeartDataManager().getPlayerHearts(player.getUniqueId());
            double newMaxHealth = storedHearts * 2.0;
            maxHealth.setBaseValue(newMaxHealth);
            
            // Save back to file to ensure name is updated
            plugin.getHeartDataManager().setPlayerHearts(player.getUniqueId(), player.getName(), storedHearts);
            plugin.getLogger().fine("[DEBUG] Loaded hearts for " + player.getName() + " in " + worldName + ": " + storedHearts);
        }
    }
}
