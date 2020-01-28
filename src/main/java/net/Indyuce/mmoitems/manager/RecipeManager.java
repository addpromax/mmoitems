package net.Indyuce.mmoitems.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.MMORecipeChoice;
import net.Indyuce.mmoitems.api.Type;

/**
 * TODO
 * When Bukkit changes their 'RecipeChoice.ExactChoice' API
 * we can remove the suppressed warnings, but right now it works
 * despite being marked as deprecated. It is just a 
 */
public class RecipeManager {
	private List<Recipe> loadedRecipes = new ArrayList<>();
	private Collection<NamespacedKey> keys = new ArrayList<>();
	
	public RecipeManager() { load(); }
	
	private void load() {
		for (Type type : MMOItems.plugin.getTypes().getAll()) {
			FileConfiguration config = type.getConfigFile().getConfig();

			for (String id : config.getKeys(false)) {
				if (config.getConfigurationSection(id).contains("advanced-craft")) {
					registerAdvancedWorkbenchRecipe(type, id, config);
				}
				
				if (config.getConfigurationSection(id).contains("crafting")) {
					ConfigurationSection craftingc = config.getConfigurationSection(id + ".crafting");
					
					if(craftingc.contains("shaped")) craftingc.getConfigurationSection("shaped").getKeys(false).forEach(recipe -> 
						registerShapedRecipe(type, id, craftingc.getStringList("shaped." + recipe)));
					if(craftingc.contains("shapeless")) craftingc.getConfigurationSection("shapeless").getKeys(false).forEach(recipe ->
						registerShapelessRecipe(type, id, craftingc.getConfigurationSection("shapeless." + recipe)));
					if(craftingc.contains("furnace")) craftingc.getConfigurationSection("furnace").getKeys(false).forEach(recipe ->
						registerFurnaceRecipe(type, id, new RecipeInformation(craftingc.getConfigurationSection("furnace." + recipe))));
					if(craftingc.contains("blast")) craftingc.getConfigurationSection("blast").getKeys(false).forEach(recipe ->
						registerBlastRecipe(type, id, new RecipeInformation(craftingc.getConfigurationSection("blast." + recipe))));
					if(craftingc.contains("smoker")) craftingc.getConfigurationSection("smoker").getKeys(false).forEach(recipe ->
						registerSmokerRecipe(type, id, new RecipeInformation(craftingc.getConfigurationSection("smoker." + recipe))));
					if(craftingc.contains("campfire")) craftingc.getConfigurationSection("campfire").getKeys(false).forEach(recipe ->
						registerCampfireRecipe(type, id, new RecipeInformation(craftingc.getConfigurationSection("campfire." + recipe))));
				}
			}
		}
		
		
		//registerCampfireRecipe(MMOItems.plugin.getItems().getItem(Type.SWORD, "SILVER_SWORD"), new RecipeChoice.ExactChoice(MMOItems.plugin.getItems().getItem(Type.get("MATERIAL"), "SILVER_INGOT")));
		
		Bukkit.getScheduler().runTask(MMOItems.plugin, new Runnable() {
			@Override
			public void run() {
				for(Recipe r : loadedRecipes)
					Bukkit.addRecipe(r);
			}
		});
	}
	
	private void registerShapedRecipe(Type type, String id, List<String> list) {
		NamespacedKey key = getRecipeKey(type, id, "shaped");
		ShapedRecipe recipe = new ShapedRecipe(key, MMOItems.plugin.getItems().getItem(type, id));
		
		List<MMORecipeChoice> rcList = MMORecipeChoice.getFromShapedConfig(list);
		if(rcList == null) return;
		
		recipe.shape("ABC", "DEF", "GHI");
		
		shapedIngredient(recipe, 'A', rcList.get(0));
		shapedIngredient(recipe, 'B', rcList.get(1));
		shapedIngredient(recipe, 'C', rcList.get(2));
		shapedIngredient(recipe, 'D', rcList.get(3));
		shapedIngredient(recipe, 'E', rcList.get(4));
		shapedIngredient(recipe, 'F', rcList.get(5));
		shapedIngredient(recipe, 'G', rcList.get(6));
		shapedIngredient(recipe, 'H', rcList.get(7));
		shapedIngredient(recipe, 'I', rcList.get(8));
		
		loadedRecipes.add(recipe); keys.add(key);
	}
	
	private void shapedIngredient(ShapedRecipe recipe, char c, MMORecipeChoice rc) {
		if(rc.isAir()) recipe.setIngredient(c, Material.AIR);
		else recipe.setIngredient(c, rc.generateChoice());
	}

	private void registerShapelessRecipe(Type type, String id, ConfigurationSection config) {
		NamespacedKey key = getRecipeKey(type, id, "shapeless");
		ShapelessRecipe recipe = new ShapelessRecipe(key, MMOItems.plugin.getItems().getItem(type, id));
		
		if(config.contains("item1")) shapelessIngredient(recipe, MMORecipeChoice.getFromString(config.getString("item1")));
		if(config.contains("item2")) shapelessIngredient(recipe, MMORecipeChoice.getFromString(config.getString("item2")));
		if(config.contains("item3")) shapelessIngredient(recipe, MMORecipeChoice.getFromString(config.getString("item3")));
		if(config.contains("item4")) shapelessIngredient(recipe, MMORecipeChoice.getFromString(config.getString("item4")));
		if(config.contains("item5")) shapelessIngredient(recipe, MMORecipeChoice.getFromString(config.getString("item5")));
		if(config.contains("item6")) shapelessIngredient(recipe, MMORecipeChoice.getFromString(config.getString("item6")));
		if(config.contains("item7")) shapelessIngredient(recipe, MMORecipeChoice.getFromString(config.getString("item7")));
		if(config.contains("item8")) shapelessIngredient(recipe, MMORecipeChoice.getFromString(config.getString("item8")));
		if(config.contains("item9")) shapelessIngredient(recipe, MMORecipeChoice.getFromString(config.getString("item9")));
		
		if(recipe.getIngredientList().isEmpty()) return;
		loadedRecipes.add(recipe); keys.add(key);
	}

	private void shapelessIngredient(ShapelessRecipe recipe, MMORecipeChoice rc) {
		if(!rc.isAir()) recipe.addIngredient(rc.generateChoice());
	}
	
	private void registerFurnaceRecipe(Type type, String id, RecipeInformation info) {
		NamespacedKey key = getRecipeKey(type, id, "furnace");
		FurnaceRecipe recipe = new FurnaceRecipe(key, MMOItems.plugin.getItems().getItem(type, id), info.choice, info.exp, info.burnTime);
		
		loadedRecipes.add(recipe); keys.add(key);
	}
	
	private void registerBlastRecipe(Type type, String id, RecipeInformation info) {
		NamespacedKey key = getRecipeKey(type, id, "blast");
		BlastingRecipe recipe = new BlastingRecipe(key, MMOItems.plugin.getItems().getItem(type, id), info.choice, info.exp, info.burnTime);
		
		loadedRecipes.add(recipe); keys.add(key);
	}
	
	private void registerSmokerRecipe(Type type, String id, RecipeInformation info) {
		NamespacedKey key = getRecipeKey(type, id, "smoker");
		SmokingRecipe recipe = new SmokingRecipe(key, MMOItems.plugin.getItems().getItem(type, id), info.choice, info.exp, info.burnTime);
		
		loadedRecipes.add(recipe); keys.add(key);
	}
	
	private void registerCampfireRecipe(Type type, String id, RecipeInformation info) {
		NamespacedKey key = getRecipeKey(type, id, "campfire");
		CampfireRecipe recipe = new CampfireRecipe(key, MMOItems.plugin.getItems().getItem(type, id), info.choice, info.exp, info.burnTime);
		
		loadedRecipes.add(recipe); keys.add(key);
	}
	
	/**
	 * @deprecated Some day I want to get proper rid of the AWB
	 * but right now we don't want to force players to update
	 * their recipes right off the bat.
	 */
	private void registerAdvancedWorkbenchRecipe(Type type, String id, FileConfiguration config) {
		MMOItems.plugin.getLogger().warning("Found deprecated adv. recipe for " + id + ". Converting it to the new system...");
		MMOItems.plugin.getLogger().warning("It is recommended to update your recipes!");
		
		NamespacedKey key = getRecipeKey(type, id, "advanced");
		ShapedRecipe recipe = new ShapedRecipe(key, MMOItems.plugin.getItems().getItem(type, id));
		recipe.shape("012", "345", "678");
		
		setIngredientOrAir(recipe, '0', config.getConfigurationSection(id + ".advanced-craft." + 0));
		setIngredientOrAir(recipe, '1', config.getConfigurationSection(id + ".advanced-craft." + 1));
		setIngredientOrAir(recipe, '2', config.getConfigurationSection(id + ".advanced-craft." + 2));
		setIngredientOrAir(recipe, '3', config.getConfigurationSection(id + ".advanced-craft." + 3));
		setIngredientOrAir(recipe, '4', config.getConfigurationSection(id + ".advanced-craft." + 4));
		setIngredientOrAir(recipe, '5', config.getConfigurationSection(id + ".advanced-craft." + 5));
		setIngredientOrAir(recipe, '6', config.getConfigurationSection(id + ".advanced-craft." + 6));
		setIngredientOrAir(recipe, '7', config.getConfigurationSection(id + ".advanced-craft." + 7));
		setIngredientOrAir(recipe, '8', config.getConfigurationSection(id + ".advanced-craft." + 8));
		
		loadedRecipes.add(recipe); keys.add(key);
	}
	
	// Just for convenience
	private NamespacedKey getRecipeKey(Type t, String i, String type) {
		return new NamespacedKey(MMOItems.plugin, "mmorecipe_" + type + "_" + t.getId() + "_" + i);
	}
	
	/**
	 * This method is purely for easily converting the AWB recipes.
	 * 
	 * @deprecated Some day I want to get proper rid of the AWB
	 * but right now we don't want to force players to update
	 * their recipes right off the bat.
	 */
	@Deprecated
	private void setIngredientOrAir(ShapedRecipe recipe, char character, ConfigurationSection c) {
		if(c.contains("type")) {
			ItemStack item = MMOItems.plugin.getItems().getItem(Type.get(c.getString("type")), c.getString("id"));
			item.setAmount(c.getInt("amount", 1));
			recipe.setIngredient(character, new RecipeChoice.ExactChoice(item));
		} else if(c.contains("material")) {
			Material material = Material.valueOf(c.getString("material"));
			int amount = c.getInt("amount", 1);
			String name = c.getString("name", "");
			if(name.isEmpty() && amount == 1)
				recipe.setIngredient(character, material);
			else {
				ItemStack item = new ItemStack(material);
				item.setAmount(amount); ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(name); item.setItemMeta(meta);
				recipe.setIngredient(character, new RecipeChoice.ExactChoice(item));
			}
		}
	}
	
	// For adding the recipes to the book
	public Collection<NamespacedKey> getNamespacedKeys() {
		return keys;
	}
	
	public void reloadRecipes() {
		Bukkit.resetRecipes();
		loadedRecipes.clear();
		keys.clear();
		load();
	}

	// For the reload command
	public int size() {
		return loadedRecipes.size();
	}
	
	class RecipeInformation {
		private final RecipeChoice choice;
		private final float exp;
		private final int burnTime;
		
		private RecipeInformation(ConfigurationSection config) {
			choice = MMORecipeChoice.getFromString(config.getString("item")).generateChoice();
			exp = (float) config.getDouble("exp", 0.35);
			burnTime = config.getInt("time", 200);
		}
	}
}
