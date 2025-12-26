package me.thetwixhunter.lifesteal;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.bukkit.ChatColor;

import java.util.List;

public class WithdrawHeartCommand implements CommandExecutor {

    private final LifeSteal plugin;

    public WithdrawHeartCommand(LifeSteal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Check if player is in an enabled world
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        if (!enabledWorlds.contains(player.getWorld().getName())) {
            sender.sendMessage(ChatColor.RED + "You can only withdraw hearts in lifesteal worlds!");
            return true;
        }

        HeartItem heartItem = new HeartItem(plugin);
        heartItem.withdrawHeart(player);
        
        return true;
    }
}
