package net.Indyuce.mmoitems.api.item.util.crafting;

import net.Indyuce.mmoitems.api.crafting.recipe.CheckedRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.item.util.ConfigItem;
import org.bukkit.Material;

public class CraftingRecipeDisplay extends ConfigItem {
    public CraftingRecipeDisplay() {
        super("CRAFTING_RECIPE_DISPLAY", Material.BARRIER, "&a&lCraft&f #name#", "{conditions}", "{conditions}&8Conditions:", "{crafting_time}",
                "{crafting_time}&7Crafting Time: &c#crafting-time#&7s", "", "&8Ingredients:", "#ingredients#", "", "&eLeft-Click to craft!",
                "&eRight-Click to preview!");
    }

    public TimedItemBuilder<CraftingRecipe> newBuilder(CheckedRecipe recipe) {
        return new TimedItemBuilder<>(this, recipe, CraftingRecipe::getOutputAmount);
    }
}
