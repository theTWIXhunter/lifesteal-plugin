package me.thetwixhunter.lifesteal;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashMap;
import java.util.Map;

public class HeartRecipe {

    private final LifeSteal plugin;

    public HeartRecipe(LifeSteal plugin) {
        this.plugin = plugin;
    }

    public void registerRecipe() {
        // Check if crafting is enabled
        if (!plugin.getConfig().getBoolean("crafting.enabled", true)) {
            plugin.getLogger().info("Heart crafting is disabled in config.");
            return;
        }

        HeartItem heartItem = new HeartItem(plugin);
        ItemStack heart = heartItem.createHeartItem();

        NamespacedKey key = new NamespacedKey(plugin, "heart_recipe");
        ShapedRecipe recipe = new ShapedRecipe(key, heart);

        try {
            // Read recipe from config
            String row1 = plugin.getConfig().getString("crafting.recipe.row1", "DIAMOND,GOLD_INGOT,DIAMOND");
            String row2 = plugin.getConfig().getString("crafting.recipe.row2", "REDSTONE,NETHER_STAR,REDSTONE");
            String row3 = plugin.getConfig().getString("crafting.recipe.row3", "DIAMOND,EMERALD,DIAMOND");

            // Parse materials and build pattern
            String[] row1Materials = row1.split(",");
            String[] row2Materials = row2.split(",");
            String[] row3Materials = row3.split(",");

            // Map to store unique materials with their keys
            Map<String, Character> materialKeys = new HashMap<>();
            char currentKey = 'A';
            StringBuilder pattern1 = new StringBuilder();
            StringBuilder pattern2 = new StringBuilder();
            StringBuilder pattern3 = new StringBuilder();

            // Process row 1
            for (String mat : row1Materials) {
                mat = mat.trim();
                if (!materialKeys.containsKey(mat)) {
                    materialKeys.put(mat, currentKey++);
                }
                pattern1.append(materialKeys.get(mat));
            }

            // Process row 2
            for (String mat : row2Materials) {
                mat = mat.trim();
                if (!materialKeys.containsKey(mat)) {
                    materialKeys.put(mat, currentKey++);
                }
                pattern2.append(materialKeys.get(mat));
            }

            // Process row 3
            for (String mat : row3Materials) {
                mat = mat.trim();
                if (!materialKeys.containsKey(mat)) {
                    materialKeys.put(mat, currentKey++);
                }
                pattern3.append(materialKeys.get(mat));
            }

            // Set the recipe shape
            recipe.shape(
                pattern1.toString(),
                pattern2.toString(),
                pattern3.toString()
            );

            // Set ingredients
            for (Map.Entry<String, Character> entry : materialKeys.entrySet()) {
                try {
                    Material material = Material.valueOf(entry.getKey());
                    recipe.setIngredient(entry.getValue(), material);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in config: " + entry.getKey());
                    return;
                }
            }

            plugin.getServer().addRecipe(recipe);
            plugin.getLogger().info("Heart crafting recipe registered successfully!");

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register heart crafting recipe: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
