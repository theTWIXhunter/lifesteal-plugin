package me.thetwixhunter.lifesteal;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class HeartItemListener implements Listener {

    private final LifeSteal plugin;
    private final HeartItem heartItem;

    public HeartItemListener(LifeSteal plugin) {
        this.plugin = plugin;
        this.heartItem = new HeartItem(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !heartItem.isHeartItem(item)) {
            return;
        }

        // Check if player is in an enabled world
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        if (!enabledWorlds.contains(player.getWorld().getName())) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        // Try to consume the heart
        if (heartItem.consumeHeart(player)) {
            // Remove one item from the stack
            item.setAmount(item.getAmount() - 1);
        }
    }
}
