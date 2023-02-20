package net.Indyuce.mmoitems.api.item.type;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.type.set.MMOTypeSet;
import net.Indyuce.mmoitems.api.item.util.identify.UnidentifiedItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * mmoitems
 * 20/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public interface MMOItemType {

    @NotNull
    String getId();

    @NotNull
    String getName();

    boolean isWeapon();

    ModifierSource getModifierSource();

    ItemStack toItemStack();

    boolean isFourGUIMode();

    String getLoreFormat();

    List<ItemStat<?, ?>> getAvailableStats();

    ConfigFile getConfiguration();

    UnidentifiedItem getUnidentifiedItem();

    void load(ConfigurationSection config);

    /**
     * Used in command executors and completions for easier manipulation
     *
     * @param id The type id
     * @return If a registered type with this ID could be found
     */
    static boolean isValid(@Nullable String id) {
        return id != null && MMOItems.plugin.getTypes().has(id.toUpperCase().replace("-", "_").replace(" ", "_"));
    }

    /**
     * Reads an ItemStack in hopes for finding its MMOItem Type.
     *
     * @param item The item to retrieve the type from
     * @return The type of the item, if it has a type.
     */
    @Nullable
    @Contract("null -> null")
    static MMOTypeSet get(@Nullable ItemStack item) {
        return get(NBTItem.get(item).getType());
    }

    /**
     * Used in command executors and completions for easier manipulation
     *
     * @param id The type id
     * @return The type or null if it couldn't be found
     */
    @Nullable
    @Contract("null -> null")
    static MMOTypeSet get(@Nullable String id) {
        String format = id.toUpperCase().replace("-", "_").replace(" ", "_");
        return MMOItems.plugin.getTypes().has(format) ? MMOItems.plugin.getTypes().get(format) : null;
    }
}
