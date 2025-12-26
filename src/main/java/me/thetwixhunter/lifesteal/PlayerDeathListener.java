package me.thetwixhunter.lifesteal;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private final LifeSteal plugin;
    private final Set<UUID> playersToEliminate = new HashSet<>();

    public PlayerDeathListener(LifeSteal plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getPlayer();
        Player killer = victim.getKiller();

        // Check if the world is enabled
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        if (!enabledWorlds.contains(victim.getWorld().getName())) {
            plugin.getLogger().info("[DEBUG] No hearts transferred: World '" + victim.getWorld().getName() + "' is not enabled for lifesteal");
            return;
        }

        // Check if there's a killer (must be a player)
        if (killer == null) {
            plugin.getLogger().info("[DEBUG] No hearts transferred: " + victim.getName() + " died without a player killer");
            return;
        }

        // Get heart configuration
        double heartAmount = plugin.getConfig().getDouble("hearts.amount", 1.0);
        double eliminateAt = plugin.getConfig().getDouble("hearts.eliminate-at", 0.5);
        double maxHearts = plugin.getConfig().getDouble("hearts.maximum", 20.0);

        // Convert hearts to health (1 heart = 2 health points)
        double healthChange = heartAmount * 2.0;
        double eliminateHealth = eliminateAt * 2.0;

        // Check if victim already has less than or equal to elimination threshold - if so, don't transfer hearts
        AttributeInstance victimMaxHealth = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (victimMaxHealth != null && victimMaxHealth.getBaseValue() <= eliminateHealth) {
            plugin.getLogger().info("[DEBUG] No hearts transferred: " + victim.getName() + " already at or below elimination threshold (" + (victimMaxHealth.getBaseValue() / 2.0) + " hearts)");
            return; // Don't transfer hearts if victim is already eliminated
        }

        // Check if killer is already at maximum hearts - if so, don't transfer hearts
        AttributeInstance killerMaxHealth = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (killerMaxHealth != null && killerMaxHealth.getBaseValue() >= maxHearts * 2.0) {
            plugin.getLogger().info("[DEBUG] No hearts transferred: " + killer.getName() + " already at maximum hearts (" + (killerMaxHealth.getBaseValue() / 2.0) + " hearts)");
            return; // Don't transfer hearts if killer is already at max
        }

        // Handle victim's health
        if (victimMaxHealth != null) {
            double oldVictimMaxHealth = victimMaxHealth.getBaseValue();
            double newVictimMaxHealth = Math.max(eliminateHealth, victimMaxHealth.getBaseValue() - healthChange);
            victimMaxHealth.setBaseValue(newVictimMaxHealth);
            
            // Store victim's new hearts
            plugin.getHeartDataManager().setPlayerHearts(victim.getUniqueId(), victim.getName(), newVictimMaxHealth / 2.0);
            
            plugin.getLogger().info("[DEBUG] Heart taken from " + victim.getName() + ": " + (oldVictimMaxHealth / 2.0) + " -> " + (newVictimMaxHealth / 2.0) + " hearts");

            // Check if victim should be eliminated (at or below threshold)
            if (newVictimMaxHealth <= eliminateHealth) {
                // Mark player for elimination when they respawn
                playersToEliminate.add(victim.getUniqueId());
                plugin.getLogger().info("[DEBUG] Player marked for elimination: " + victim.getName() + " (" + (newVictimMaxHealth / 2.0) + " hearts)");
            }
        }

        // Handle killer's health
        if (killerMaxHealth != null) {
            double oldKillerMaxHealth = killerMaxHealth.getBaseValue();
            double newKillerMaxHealth = Math.min(maxHearts * 2.0, killerMaxHealth.getBaseValue() + healthChange);
            killerMaxHealth.setBaseValue(newKillerMaxHealth);
            
            // Heal the killer to their new max health
            killer.setHealth(newKillerMaxHealth);
            
            // Store killer's new hearts
            plugin.getHeartDataManager().setPlayerHearts(killer.getUniqueId(), killer.getName(), newKillerMaxHealth / 2.0);
            
            if (oldKillerMaxHealth == newKillerMaxHealth) {
                plugin.getLogger().info("[DEBUG] No heart added to " + killer.getName() + ": Already at maximum hearts (" + (newKillerMaxHealth / 2.0) + " hearts)");
            } else {
                plugin.getLogger().info("[DEBUG] Heart added to " + killer.getName() + ": " + (oldKillerMaxHealth / 2.0) + " -> " + (newKillerMaxHealth / 2.0) + " hearts");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Check if this player should be eliminated
        if (playersToEliminate.remove(player.getUniqueId())) {
            plugin.getLogger().info("[DEBUG] Player respawned for elimination: " + player.getName() + " - Scheduling commands execution");
            // Execute elimination commands after a short delay to ensure player is fully respawned
            Bukkit.getScheduler().runTaskLater(plugin, () -> executeEliminationCommands(player), 5L);
        }
    }

    private void executeEliminationCommands(Player player) {
        List<String> commands = plugin.getConfig().getStringList("elimination-commands");
        
        plugin.getLogger().info("[DEBUG] Executing " + commands.size() + " elimination command(s) for " + player.getName());
        
        for (String command : commands) {
            // Replace placeholder
            String processedCommand = command.replace("%player%", player.getName());
            
            // Execute command with operator permissions
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    // Execute the command as console
                    plugin.getLogger().info("[DEBUG] Executing command: " + processedCommand);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                    plugin.getLogger().info("[DEBUG] Command executed successfully: " + processedCommand);
                } catch (Exception e) {
                    plugin.getLogger().warning("[DEBUG] Failed to execute elimination command: " + processedCommand);
                    e.printStackTrace();
                }
            });
        }
    }
}
