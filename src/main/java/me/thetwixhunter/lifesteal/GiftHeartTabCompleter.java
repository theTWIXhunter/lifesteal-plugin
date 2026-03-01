package me.thetwixhunter.lifesteal;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GiftHeartTabCompleter implements TabCompleter {

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - player name
            // Return null to show default online players
            return null;
        } else if (args.length == 2) {
            // Second argument - amount
            completions.add("0.5");
            completions.add("1.0");
            completions.add("2.0");
            completions.add("5.0");
            completions.add("10.0");
        }

        return completions;
    }
}
