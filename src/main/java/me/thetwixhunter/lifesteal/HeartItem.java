package me.thetwixhunter.lifesteal;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class HeartItem {

    private final LifeSteal plugin;

    public HeartItem(LifeSteal plugin) {
        this.plugin = plugin;
    }

    public ItemStack createHeartItem() {
        ItemStack item = new ItemStack(Material.SPIDER_EYE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "❤ Heart");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Right-click to consume",
                ChatColor.GRAY + "and gain +1 max heart"
            ));
            
            // Set custom model data for resource pack
            meta.setCustomModelData(1001);
            
            // Add persistent data to identify this as a heart item
            NamespacedKey key = new NamespacedKey(plugin, "lifesteal_heart");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            
            item.setItemMeta(meta);
            
            // Debug log to verify custom model data
            plugin.getLogger().info("Created heart item with custom model data: " + item.getItemMeta().getCustomModelData());
        }
        
        return item;
    }

    public boolean isHeartItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "lifesteal_heart");
        
        // Check for persistent data (always reliable)
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN)) {
            return true;
        }
        
        // Also check custom model data as fallback (for manually given items)
        if (meta.hasCustomModelData() && meta.getCustomModelData() == 1001) {
            return true;
        }
        
        return false;
    }

    public boolean consumeHeart(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth == null) {
            return false;
        }

        double maxHearts = plugin.getConfig().getDouble("hearts.maximum", 20.0);
        double currentHearts = maxHealth.getBaseValue() / 2.0;

        if (currentHearts >= maxHearts) {
            player.sendMessage(ChatColor.RED + "You already have the maximum amount of hearts!");
            return false;
        }

        // Add 1 heart
        double newMaxHealth = Math.min(maxHearts * 2.0, maxHealth.getBaseValue() + 2.0);
        maxHealth.setBaseValue(newMaxHealth);
        
        // Update stored data
        plugin.getHeartDataManager().setPlayerHearts(player.getUniqueId(), player.getName(), newMaxHealth / 2.0);
        
        player.sendMessage(ChatColor.GREEN + "You consumed a heart! New max hearts: " + (newMaxHealth / 2.0));
        plugin.getLogger().info("[DEBUG] " + player.getName() + " consumed a heart: " + currentHearts + " -> " + (newMaxHealth / 2.0));
        
        return true;
    }

    public boolean withdrawHeart(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth == null) {
            return false;
        }

        double eliminateAt = plugin.getConfig().getDouble("hearts.eliminate-at", 0.5);
        double currentHearts = maxHealth.getBaseValue() / 2.0;

        // Check if player would go below elimination threshold
        if (currentHearts - 1.0 <= eliminateAt) {
            player.sendMessage(ChatColor.RED + "You cannot withdraw hearts below " + (eliminateAt + 1.0) + " hearts!");
            return false;
        }

        // Remove 1 heart
        double newMaxHealth = maxHealth.getBaseValue() - 2.0;
        maxHealth.setBaseValue(newMaxHealth);
        
        // Update stored data
        plugin.getHeartDataManager().setPlayerHearts(player.getUniqueId(), player.getName(), newMaxHealth / 2.0);
        
        // Give player the heart item
        ItemStack heartItem = createHeartItem();
        player.getInventory().addItem(heartItem);
        
        player.sendMessage(ChatColor.GREEN + "You withdrew a heart! New max hearts: " + (newMaxHealth / 2.0));
        plugin.getLogger().info("[DEBUG] " + player.getName() + " withdrew a heart: " + currentHearts + " -> " + (newMaxHealth / 2.0));
        
        return true;
    }
}
