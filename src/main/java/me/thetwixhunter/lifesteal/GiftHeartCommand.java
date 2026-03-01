package me.thetwixhunter.lifesteal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiftHeartCommand implements CommandExecutor {

    private final LifeSteal plugin;

    public GiftHeartCommand(LifeSteal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        MessageManager msg = plugin.getMessageManager();
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg.getMessage("only-players"));
            return true;
        }

        Player giver = (Player) sender;

        // Check arguments
        if (args.length < 2) {
            sender.sendMessage(msg.getMessage("giftheart.usage"));
            return true;
        }

        // Parse amount
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                sender.sendMessage(msg.getMessage("must-be-positive"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(msg.getMessage("invalid-number"));
            return true;
        }

        // Check if giver is in an enabled world
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        if (!enabledWorlds.contains(giver.getWorld().getName())) {
            sender.sendMessage(msg.getMessage("giftheart.only-in-lifesteal-worlds"));
            return true;
        }

        // Check if giver has enough hearts
        AttributeInstance giverMaxHealth = giver.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (giverMaxHealth == null) {
            return true;
        }

        double giverHearts = giverMaxHealth.getBaseValue() / 2.0;
        double eliminateAt = plugin.getConfig().getDouble("hearts.eliminate-at", 0.5);

        if (giverHearts - amount <= eliminateAt) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.valueOf(amount + eliminateAt));
            placeholders.put("gift", String.valueOf(amount));
            sender.sendMessage(msg.getMessage("giftheart.not-enough-hearts", placeholders));
            return true;
        }

        // Find target player
        Player target = Bukkit.getPlayerExact(args[0]);
        
        // Remove hearts from giver
        double newGiverMaxHealth = giverMaxHealth.getBaseValue() - (amount * 2.0);
        giverMaxHealth.setBaseValue(newGiverMaxHealth);
        plugin.getHeartDataManager().setPlayerHearts(giver.getUniqueId(), giver.getName(), newGiverMaxHealth / 2.0);
        
        // Target is online
        if (target != null) {
            boolean isInEnabledWorld = enabledWorlds.contains(target.getWorld().getName());
            
            if (isInEnabledWorld) {
                // Add hearts directly to the player
                AttributeInstance targetMaxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (targetMaxHealth != null) {
                    double targetHearts = targetMaxHealth.getBaseValue() / 2.0;
                    double maxHearts = plugin.getConfig().getDouble("hearts.maximum", 20.0);
                    
                    // Check if target is eliminated (at or below elimination threshold)
                    boolean wasEliminated = targetHearts <= eliminateAt;
                    
                    if (wasEliminated) {
                        // Revive the player
                        double newTargetMaxHealth = amount * 2.0;
                        newTargetMaxHealth = Math.min(maxHearts * 2.0, newTargetMaxHealth);
                        targetMaxHealth.setBaseValue(newTargetMaxHealth);
                        
                        // Heal the player to full health
                        target.setHealth(newTargetMaxHealth);
                        
                        plugin.getHeartDataManager().setPlayerHearts(target.getUniqueId(), target.getName(), newTargetMaxHealth / 2.0);
                        
                        // Run revive commands
                        List<String> reviveCommands = plugin.getConfig().getStringList("revive.commands");
                        for (String cmd : reviveCommands) {
                            String reviveCmd = cmd.replace("%player%", target.getName());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), reviveCmd);
                        }
                        
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("player", giver.getName());
                        placeholders.put("hearts", String.valueOf(newTargetMaxHealth / 2.0));
                        target.sendMessage(msg.getMessage("giftheart.revived-target", placeholders));
                        
                        placeholders.clear();
                        placeholders.put("amount", String.valueOf(amount));
                        placeholders.put("player", target.getName());
                        placeholders.put("hearts", String.valueOf(newTargetMaxHealth / 2.0));
                        giver.sendMessage(msg.getMessage("giftheart.revived-sender", placeholders));
                        plugin.getLogger().info(giver.getName() + " gifted " + amount + " hearts to " + target.getName() + " and revived them");
                    } else {
                        // Normal gift - add hearts
                        double newTargetMaxHealth = Math.min(maxHearts * 2.0, targetMaxHealth.getBaseValue() + (amount * 2.0));
                        targetMaxHealth.setBaseValue(newTargetMaxHealth);
                        plugin.getHeartDataManager().setPlayerHearts(target.getUniqueId(), target.getName(), newTargetMaxHealth / 2.0);
                        
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("amount", String.valueOf(amount));
                        placeholders.put("player", giver.getName());
                        placeholders.put("hearts", String.valueOf(newTargetMaxHealth / 2.0));
                        target.sendMessage(msg.getMessage("giftheart.received", placeholders));
                        
                        placeholders.clear();
                        placeholders.put("amount", String.valueOf(amount));
                        placeholders.put("player", target.getName());
                        giver.sendMessage(msg.getMessage("giftheart.sent", placeholders));
                        plugin.getLogger().info(giver.getName() + " gifted " + amount + " hearts to " + target.getName());
                    }
                }
            } else {
                // Target is online but not in an enabled world - add to file only
                double targetHearts = plugin.getHeartDataManager().getPlayerHearts(target.getUniqueId());
                double maxHearts = plugin.getConfig().getDouble("hearts.maximum", 20.0);
                
                // Check if target is eliminated
                boolean wasEliminated = targetHearts <= eliminateAt;
                
                if (wasEliminated) {
                    // Revive the player
                    double newTargetHearts = Math.min(maxHearts, amount);
                    plugin.getHeartDataManager().setPlayerHearts(target.getUniqueId(), target.getName(), newTargetHearts);
                    
                    // Run revive commands
                    List<String> reviveCommands = plugin.getConfig().getStringList("revive.commands");
                    for (String cmd : reviveCommands) {
                        String reviveCmd = cmd.replace("%player%", target.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), reviveCmd);
                    }
                    
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", giver.getName());
                    placeholders.put("hearts", String.valueOf(newTargetHearts));
                    target.sendMessage(msg.getMessage("giftheart.revived-target-world", placeholders));
                    
                    placeholders.clear();
                    placeholders.put("amount", String.valueOf(amount));
                    placeholders.put("player", target.getName());
                    placeholders.put("hearts", String.valueOf(newTargetHearts));
                    giver.sendMessage(msg.getMessage("giftheart.revived-sender-world", placeholders));
                    plugin.getLogger().info(giver.getName() + " gifted " + amount + " hearts to " + target.getName() + " (offline/out of world) and revived them");
                } else {
                    // Normal gift
                    double newTargetHearts = Math.min(maxHearts, targetHearts + amount);
                    plugin.getHeartDataManager().setPlayerHearts(target.getUniqueId(), target.getName(), newTargetHearts);
                    
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("amount", String.valueOf(amount));
                    placeholders.put("player", giver.getName());
                    placeholders.put("hearts", String.valueOf(newTargetHearts));
                    target.sendMessage(msg.getMessage("giftheart.received-world", placeholders));
                    
                    placeholders.clear();
                    placeholders.put("amount", String.valueOf(amount));
                    placeholders.put("player", target.getName());
                    giver.sendMessage(msg.getMessage("giftheart.sent", placeholders));
                    plugin.getLogger().info(giver.getName() + " gifted " + amount + " hearts to " + target.getName() + " (out of enabled world)");
                }
            }
        } else {
            // Target is offline - add to file only
            double targetHearts = plugin.getHeartDataManager().getPlayerHearts(Bukkit.getOfflinePlayer(args[0]).getUniqueId());
            double maxHearts = plugin.getConfig().getDouble("hearts.maximum", 20.0);
            
            // Check if target is eliminated
            boolean wasEliminated = targetHearts <= eliminateAt;
            
            if (wasEliminated) {
                // Revive the player
                double newTargetHearts = Math.min(maxHearts, amount);
                plugin.getHeartDataManager().setPlayerHearts(Bukkit.getOfflinePlayer(args[0]).getUniqueId(), args[0], newTargetHearts);
                
                // Run revive commands
                List<String> reviveCommands = plugin.getConfig().getStringList("revive.commands");
                for (String cmd : reviveCommands) {
                    String reviveCmd = cmd.replace("%player%", args[0]);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), reviveCmd);
                }
                
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("amount", String.valueOf(amount));
                placeholders.put("player", args[0]);
                placeholders.put("hearts", String.valueOf(newTargetHearts));
                giver.sendMessage(msg.getMessage("giftheart.revived-offline", placeholders));
                plugin.getLogger().info(giver.getName() + " gifted " + amount + " hearts to " + args[0] + " (offline) and revived them");
            } else {
                // Normal gift
                double newTargetHearts = Math.min(maxHearts, targetHearts + amount);
                plugin.getHeartDataManager().setPlayerHearts(Bukkit.getOfflinePlayer(args[0]).getUniqueId(), args[0], newTargetHearts);
                
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("amount", String.valueOf(amount));
                placeholders.put("player", args[0]);
                placeholders.put("hearts", String.valueOf(newTargetHearts));
                giver.sendMessage(msg.getMessage("giftheart.sent-offline", placeholders));
                plugin.getLogger().info(giver.getName() + " gifted " + amount + " hearts to " + args[0] + " (offline)");
            }
        }
        
        return true;
    }
}
