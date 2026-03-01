package me.thetwixhunter.lifesteal;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HeartDataManager {

    private final LifeSteal plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public HeartDataManager(LifeSteal plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml!");
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void reloadData() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        plugin.getLogger().info("Reloaded data.yml from disk");
    }

    public void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml!");
            e.printStackTrace();
        }
    }

    public void setPlayerHearts(UUID uuid, String name, double hearts) {
        dataConfig.set("players." + uuid.toString() + ".name", name);
        dataConfig.set("players." + uuid.toString() + ".hearts", hearts);
        saveData();
        plugin.getLogger().fine("[DEBUG] Stored hearts for " + name + ": " + hearts);
    }

    public double getPlayerHearts(UUID uuid) {
        double defaultHearts = plugin.getConfig().getDouble("hearts.starting", 10.0);
        return dataConfig.getDouble("players." + uuid.toString() + ".hearts", defaultHearts);
    }

    public String getPlayerName(UUID uuid) {
        return dataConfig.getString("players." + uuid.toString() + ".name", "Unknown");
    }

    public List<PlayerHeartEntry> getTopPlayers(int limit) {
        List<PlayerHeartEntry> players = new ArrayList<>();
        
        if (dataConfig.getConfigurationSection("players") == null) {
            return players;
        }
        
        for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                String name = getPlayerName(uuid);
                double hearts = getPlayerHearts(uuid);
                players.add(new PlayerHeartEntry(uuid, name, hearts));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in data.yml: " + uuidString);
            }
        }
        
        return players.stream()
                .sorted((a, b) -> Double.compare(b.getHearts(), a.getHearts()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public static class PlayerHeartEntry {
        private final UUID uuid;
        private final String name;
        private final double hearts;

        public PlayerHeartEntry(UUID uuid, String name, double hearts) {
            this.uuid = uuid;
            this.name = name;
            this.hearts = hearts;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public double getHearts() {
            return hearts;
        }
    }
}
