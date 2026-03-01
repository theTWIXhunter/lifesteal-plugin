package me.thetwixhunter.lifesteal;

import org.bukkit.plugin.java.JavaPlugin;

public class LifeSteal extends JavaPlugin {

    private static LifeSteal instance;
    private HeartDataManager heartDataManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        heartDataManager = new HeartDataManager(this);
        messageManager = new MessageManager(this);
        
        // Register heart crafting recipe
        HeartRecipe heartRecipe = new HeartRecipe(this);
        heartRecipe.registerRecipe();
        
        // Register event listener
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new HeartItemListener(this), this);
        
        // Register commands
        getCommand("lifestealplugin").setExecutor(new LifeStealCommand(this));
        getCommand("withdrawheart").setExecutor(new WithdrawHeartCommand(this));
        getCommand("giftheart").setExecutor(new GiftHeartCommand(this));
        
        // Register tab completers
        getCommand("lifestealplugin").setTabCompleter(new LifeStealTabCompleter());
        getCommand("giftheart").setTabCompleter(new GiftHeartTabCompleter());
        
        // Register PlaceholderAPI expansion if available
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LifeStealPlaceholders(this).register();
            getLogger().info("PlaceholderAPI hooked successfully!");
        } else {
            getLogger().info("PlaceholderAPI not found. Placeholders will not be available.");
        }
        
        getLogger().info("LifeSteal plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LifeSteal plugin has been disabled!");
    }

    public static LifeSteal getInstance() {
        return instance;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public HeartDataManager getHeartDataManager() {
        return heartDataManager;
    }
}
