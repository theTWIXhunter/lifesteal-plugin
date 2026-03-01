package me.thetwixhunter.lifesteal;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LifeStealTabCompleter implements TabCompleter {

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - show all available subcommands based on permissions
            List<String> subcommands = new ArrayList<>();
            
            if (sender.hasPermission("lifesteal.reload")) {
                subcommands.add("reload");
            }
            if (sender.hasPermission("lifesteal.revive")) {
                subcommands.add("revive");
            }
            if (sender.hasPermission("lifesteal.withdrawheart")) {
                subcommands.add("withdrawheart");
            }
            if (sender.hasPermission("lifesteal.sethearts")) {
                subcommands.add("sethearts");
            }
            
            // Filter based on what user is typing
            String input = args[0].toLowerCase();
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(input)) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            // Second argument
            String subcommand = args[0].toLowerCase();
            
            if (subcommand.equals("revive") || subcommand.equals("sethearts")) {
                // Show online player names
                return null; // null returns default player list
            }
        } else if (args.length == 3) {
            String subcommand = args[0].toLowerCase();
            
            if (subcommand.equals("sethearts")) {
                // Show amount suggestions
                completions.add("1.0");
                completions.add("5.0");
                completions.add("10.0");
                completions.add("20.0");
            }
        }

        return completions;
    }
}
