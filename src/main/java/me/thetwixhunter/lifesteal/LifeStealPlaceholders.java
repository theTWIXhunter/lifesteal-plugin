package me.thetwixhunter.lifesteal;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class LifeStealPlaceholders extends PlaceholderExpansion {

    private final LifeSteal plugin;

    public LifeStealPlaceholders(LifeSteal plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "lifesteal";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "me.thetwixhunter";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    @Nullable
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.startsWith("top_")) {
            String[] parts = params.split("_");
            if (parts.length == 3) {
                try {
                    int position = Integer.parseInt(parts[1]);
                    String type = parts[2];
                    
                    if (position < 1 || position > 5) {
                        return "Invalid position";
                    }
                    
                    List<PlayerHeartData> topPlayers = getTopPlayers();
                    
                    if (position > topPlayers.size()) {
                        return type.equals("name") ? "N/A" : "0.0";
                    }
                    
                    PlayerHeartData data = topPlayers.get(position - 1);
                    
                    if (type.equals("name")) {
                        return data.getName();
                    } else if (type.equals("maxhearts")) {
                        return String.format("%.1f", data.getHearts());
                    }
                } catch (NumberFormatException e) {
                    return "Invalid position";
                }
            }
        }
        
        return null;
    }

    private List<PlayerHeartData> getTopPlayers() {
        List<HeartDataManager.PlayerHeartEntry> entries = plugin.getHeartDataManager().getTopPlayers(5);
        List<PlayerHeartData> players = new ArrayList<>();
        
        for (HeartDataManager.PlayerHeartEntry entry : entries) {
            players.add(new PlayerHeartData(entry.getName(), entry.getHearts()));
        }
        
        return players;
    }

    private static class PlayerHeartData {
        private final String name;
        private final double hearts;

        public PlayerHeartData(String name, double hearts) {
            this.name = name;
            this.hearts = hearts;
        }

        public String getName() {
            return name;
        }

        public double getHearts() {
            return hearts;
        }
    }
}
