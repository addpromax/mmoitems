package net.Indyuce.mmoitems.api.item.util.crafting;

import net.Indyuce.mmoitems.api.crafting.recipe.CheckedRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.UpgradingRecipe;
import net.Indyuce.mmoitems.api.item.util.ConfigItem;
import org.bukkit.Material;

public class UpgradingRecipeDisplay extends ConfigItem {
    public UpgradingRecipeDisplay() {
        super("UPGRADING_RECIPE_DISPLAY", Material.BARRIER, "&e&lUpgrade&f #name#", "{conditions}", "{conditions}&8Conditions:", "{crafting_time}",
                "{crafting_time}&7Crafting Time: &c#crafting-time#&7s", "", "&8Ingredients:",
                "#ingredients#", "", "&eLeft-Click to craft!", "&eRight-Click to preview!");
    }

    public TimedItemBuilder<UpgradingRecipe> newBuilder(CheckedRecipe recipe) {
        return new TimedItemBuilder<>(this, recipe, upgradingRecipe -> 1);
    }
}
