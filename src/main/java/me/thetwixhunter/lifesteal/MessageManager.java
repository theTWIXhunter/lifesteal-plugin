package me.thetwixhunter.lifesteal;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final LifeSteal plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public MessageManager(LifeSteal plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        // Get messages file name from config
        String messagesFileName = plugin.getConfig().getString("messages-file", "messages.yml");
        messagesFile = new File(plugin.getDataFolder(), messagesFileName);

        // Save default messages.yml if it doesn't exist
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        // Load messages configuration
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Load defaults from jar
        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            messagesConfig.setDefaults(defaultConfig);
        }
    }

    public void reloadMessages() {
        loadMessages();
    }

    /**
     * Get a message from the messages file
     * @param path The path to the message in the YAML file (e.g., "giftheart.usage")
     * @return The formatted message with color codes
     */
    public String getMessage(String path) {
        String message = messagesConfig.getString(path);
        if (message == null) {
            plugin.getLogger().warning("Message not found: " + path);
            return ChatColor.RED + "Message not found: " + path;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Get a message with placeholder replacements
     * @param path The path to the message in the YAML file
     * @param placeholders Map of placeholder keys to values (without % symbols)
     * @return The formatted message with placeholders replaced
     */
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        return message;
    }

    /**
     * Convenience method to get a message with a single placeholder
     */
    public String getMessage(String path, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        return getMessage(path, placeholders);
    }

    /**
     * Convenience method to get a message with two placeholders
     */
    public String getMessage(String path, String placeholder1, String value1, String placeholder2, String value2) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder1, value1);
        placeholders.put(placeholder2, value2);
        return getMessage(path, placeholders);
    }
}
