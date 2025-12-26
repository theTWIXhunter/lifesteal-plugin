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

import java.util.List;

public class GiftHeartCommand implements CommandExecutor {

    private final LifeSteal plugin;

    public GiftHeartCommand(LifeSteal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player giver = (Player) sender;

        // Check arguments
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /giftheart <player> <amount>");
            return true;
        }

        // Parse amount
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                sender.sendMessage(ChatColor.RED + "Amount must be positive!");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount! Please enter a number.");
            return true;
        }

        // Check if giver is in an enabled world
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        if (!enabledWorlds.contains(giver.getWorld().getName())) {
            sender.sendMessage(ChatColor.RED + "You can only gift hearts in lifesteal worlds!");
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
            sender.sendMessage(ChatColor.RED + "You don't have enough hearts! You need at least " + (amount + eliminateAt) + " hearts to gift " + amount + " hearts.");
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
                        
                        target.sendMessage(ChatColor.GREEN + "You have been revived by " + giver.getName() + "! You received " + (newTargetMaxHealth / 2.0) + " hearts!");
                        giver.sendMessage(ChatColor.GREEN + "You gifted " + amount + " hearts to " + target.getName() + " and revived them! They now have " + (newTargetMaxHealth / 2.0) + " hearts.");
                        plugin.getLogger().info(giver.getName() + " gifted " + amount + " hearts to " + target.getName() + " and revived them");
                    } else {
                        // Normal gift - add hearts
                        double newTargetMaxHealth = Math.min(maxHearts * 2.0, targetMaxHealth.getBaseValue() + (amount * 2.0));
                        targetMaxHealth.setBaseValue(newTargetMaxHealth);
                        plugin.getHeartDataManager().setPlayerHearts(target.getUniqueId(), target.getName(), newTargetMaxHealth / 2.0);
                        
                        target.sendMessage(ChatColor.GREEN + "You received " + amount + " hearts from " + giver.getName() + "! New max hearts: " + (newTargetMaxHealth / 2.0));
                        giver.sendMessage(ChatColor.GREEN + "You gifted " + amount + " hearts to " + target.getName() + "!");
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
                    
                    target.sendMessage(ChatColor.GREEN + "You have been revived by " + giver.getName() + "! You will have " + newTargetHearts + " hearts when you join a lifesteal world!");
                    giver.sendMessage(ChatColor.GREEN + "You gifted " + amount + " hearts to " + target.getName() + " and revived them! They will have " + newTargetHearts + " hearts when they join a lifesteal world.");
                    plugin.getLogger().info(giver.getName() + " gifted " + amount + " hearts to " + target.getName() + " (offline/out of world) and revived them");
                } else {
                    // Normal gift
                    double newTargetHearts = Math.min(maxHearts, targetHearts + amount);
                    plugin.getHeartDataManager().setPlayerHearts(target.getUniqueId(), target.getName(), newTargetHearts);
                    
                    target.sendMessage(ChatColor.GREEN + "You received " + amount + " hearts from " + giver.getName() + "! You will have " + newTargetHearts + " max hearts when you join a lifesteal world.");
                    giver.sendMessage(ChatColor.GREEN + "You gifted " + amount + " hearts to " + target.getName() + "!");
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
                
                giver.sendMessage(ChatColor.GREEN + "You gifted " + amount + " hearts to " + args[0] + " and revived them! They will have " + newTargetHearts + " hearts when they join.");
                plugin.getLogger().info(giver.getName() + " gifted " + amount + " hearts to " + args[0] + " (offline) and revived them");
            } else {
                // Normal gift
                double newTargetHearts = Math.min(maxHearts, targetHearts + amount);
                plugin.getHeartDataManager().setPlayerHearts(Bukkit.getOfflinePlayer(args[0]).getUniqueId(), args[0], newTargetHearts);
                
                giver.sendMessage(ChatColor.GREEN + "You gifted " + amount + " hearts to " + args[0] + " (offline)! They will have " + newTargetHearts + " max hearts when they join.");
                plugin.getLogger().info(giver.getName() + " gifted " + amount + " hearts to " + args[0] + " (offline)");
            }
        }
        
        return true;
    }
}
