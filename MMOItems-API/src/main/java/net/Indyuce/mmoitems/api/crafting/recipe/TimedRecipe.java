package net.Indyuce.mmoitems.api.crafting.recipe;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * mmoitems
 * 10/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 * <p>
 * Represents a recipe that can be crafted in a crafting station
 * which has a crafting time.
 */
public abstract class TimedRecipe extends Recipe {

    /*
     * There can't be any crafting time for upgrading recipes since there is no
     * way to save an MMOItem in the config file TODO save as ItemStack
     */
    private final double craftingTime;


    public TimedRecipe(@NotNull ConfigurationSection config) {
        super(config);
        craftingTime = config.getDouble("crafting-time");
    }

    public double getCraftingTime() {
        return craftingTime;
    }

    public boolean isInstant() {
        return craftingTime <= 0;
    }
}
