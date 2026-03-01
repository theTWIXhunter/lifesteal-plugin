package me.thetwixhunter.lifesteal;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LifeStealCommand implements CommandExecutor {

    private final LifeSteal plugin;

    public LifeStealCommand(LifeSteal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        MessageManager msg = plugin.getMessageManager();
        
        if (args.length == 0) {
            sender.sendMessage(msg.getMessage("lifestealplugin.usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("lifesteal.reload")) {
                sender.sendMessage(msg.getMessage("lifestealplugin.reload.no-permission"));
                return true;
            }

            plugin.reloadConfig();
            
            // Reload data.yml from disk
            plugin.getHeartDataManager().reloadData();
            
            // Reload hearts from file for all online players in enabled worlds
            java.util.List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
            int playersReloaded = 0;
            
            for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                if (enabledWorlds.contains(player.getWorld().getName())) {
                    org.bukkit.attribute.AttributeInstance maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
                    if (maxHealth != null) {
                        double storedHearts = plugin.getHeartDataManager().getPlayerHearts(player.getUniqueId());
                        double newMaxHealth = storedHearts * 2.0;
                        maxHealth.setBaseValue(newMaxHealth);
                        
                        // Adjust current health if it exceeds new max
                        if (player.getHealth() > newMaxHealth) {
                            player.setHealth(newMaxHealth);
                        }
                        
                        playersReloaded++;
                        plugin.getLogger().fine("[DEBUG] Reloaded hearts for " + player.getName() + ": " + storedHearts);
                    }
                }
            }
            
            sender.sendMessage(msg.getMessage("lifestealplugin.reload.success"));
            if (playersReloaded > 0) {
                sender.sendMessage(msg.getMessage("lifestealplugin.reload.players-reloaded", "amount", String.valueOf(playersReloaded)));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("revive")) {
            if (!sender.hasPermission("lifesteal.revive")) {
                sender.sendMessage(msg.getMessage("lifestealplugin.revive.no-permission"));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(msg.getMessage("lifestealplugin.revive.usage"));
                return true;
            }

            String playerName = args[1];
            org.bukkit.entity.Player target = plugin.getServer().getPlayer(playerName);

            if (target == null || !target.isOnline()) {
                sender.sendMessage(msg.getMessage("lifestealplugin.revive.player-not-online", "player", playerName));
                return true;
            }

            revivePlayer(target, sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("withdrawheart")) {
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage(msg.getMessage("only-players"));
                return true;
            }

            if (!sender.hasPermission("lifesteal.withdrawheart")) {
                sender.sendMessage(msg.getMessage("lifestealplugin.withdrawheart.no-permission"));
                return true;
            }

            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;

            // Check if player is in an enabled world
            java.util.List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
            if (!enabledWorlds.contains(player.getWorld().getName())) {
                sender.sendMessage(msg.getMessage("withdrawheart.only-in-lifesteal-worlds"));
                return true;
            }

            HeartItem heartItem = new HeartItem(plugin);
            if (heartItem.withdrawHeart(player)) {
                return true;
            } else {
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("sethearts")) {
            if (!sender.hasPermission("lifesteal.sethearts")) {
                sender.sendMessage(msg.getMessage("lifestealplugin.sethearts.no-permission"));
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(msg.getMessage("lifestealplugin.sethearts.usage"));
                return true;
            }

            String playerName = args[1];
            double amount;
            
            try {
                amount = Double.parseDouble(args[2]);
                if (amount <= 0) {
                    sender.sendMessage(msg.getMessage("must-be-positive"));
                    return true;
                }
                
                double maxHearts = plugin.getConfig().getDouble("hearts.maximum", 20.0);
                if (amount > maxHearts) {
                    sender.sendMessage(msg.getMessage("lifestealplugin.sethearts.exceeds-maximum", "max", String.valueOf(maxHearts)));
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(msg.getMessage("invalid-number"));
                return true;
            }

            org.bukkit.entity.Player target = plugin.getServer().getPlayerExact(playerName);
            
            if (target != null) {
                // Player is online - update file
                plugin.getHeartDataManager().setPlayerHearts(target.getUniqueId(), target.getName(), amount);
                
                // Check if player is in an enabled world and update their hearts directly
                java.util.List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
                if (enabledWorlds.contains(target.getWorld().getName())) {
                    org.bukkit.attribute.AttributeInstance maxHealth = target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
                    if (maxHealth != null) {
                        double newMaxHealth = amount * 2.0;
                        maxHealth.setBaseValue(newMaxHealth);
                        
                        // Adjust current health if it exceeds new max
                        if (target.getHealth() > newMaxHealth) {
                            target.setHealth(newMaxHealth);
                        }
                        
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("player", target.getName());
                        placeholders.put("amount", String.valueOf(amount));
                        sender.sendMessage(msg.getMessage("lifestealplugin.sethearts.success-immediate", placeholders));
                        target.sendMessage(msg.getMessage("lifestealplugin.sethearts.target-immediate", "amount", String.valueOf(amount)));
                    }
                } else {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", target.getName());
                    placeholders.put("amount", String.valueOf(amount));
                    sender.sendMessage(msg.getMessage("lifestealplugin.sethearts.success-on-join", placeholders));
                    target.sendMessage(msg.getMessage("lifestealplugin.sethearts.target-on-join", "amount", String.valueOf(amount)));
                }
            } else {
                // Player is offline - update file only
                org.bukkit.OfflinePlayer offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(playerName);
                plugin.getHeartDataManager().setPlayerHearts(offlinePlayer.getUniqueId(), playerName, amount);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", playerName);
                placeholders.put("amount", String.valueOf(amount));
                sender.sendMessage(msg.getMessage("lifestealplugin.sethearts.success-offline", placeholders));
            }
            
            return true;
        }

        sender.sendMessage(msg.getMessage("lifestealplugin.unknown-subcommand"));
        return true;
    }

    private void revivePlayer(org.bukkit.entity.Player player, CommandSender sender) {
        MessageManager msg = plugin.getMessageManager();
        double reviveHearts = plugin.getConfig().getDouble("revive.hearts", 10.0);
        double newMaxHealth = reviveHearts * 2.0; // Convert hearts to health points

        org.bukkit.attribute.AttributeInstance maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(newMaxHealth);
            player.setHealth(newMaxHealth); // Heal player to full
            
            // Store player's new hearts
            plugin.getHeartDataManager().setPlayerHearts(player.getUniqueId(), player.getName(), reviveHearts);
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", player.getName());
            placeholders.put("hearts", String.valueOf(reviveHearts));
            sender.sendMessage(msg.getMessage("lifestealplugin.revive.success", placeholders));
            plugin.getLogger().info("[DEBUG] Player revived: " + player.getName() + " - Set to " + reviveHearts + " hearts");

            // Execute revive commands
            java.util.List<String> commands = plugin.getConfig().getStringList("revive.commands");
            for (String cmd : commands) {
                String processedCommand = cmd.replace("%player%", player.getName());
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        plugin.getLogger().info("[DEBUG] Executing revive command: " + processedCommand);
                        org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), processedCommand);
                    } catch (Exception e) {
                        plugin.getLogger().warning("[DEBUG] Failed to execute revive command: " + processedCommand);
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
