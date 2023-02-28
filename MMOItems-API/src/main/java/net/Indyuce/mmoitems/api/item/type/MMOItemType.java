package net.Indyuce.mmoitems.api.item.type;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.util.identify.UnidentifiedItem;
import net.Indyuce.mmoitems.manager.TypeManager;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * mmoitems
 * 24/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class MMOItemType {

    private final String id;
    private final String name;
    private final ModifierSource modifierSource;
    private final boolean weapon;
    private final String loreFormat;
    private final ItemStack item;
    private final UnidentifiedItem unidentifiedTemplate;
    private final List<ItemStat<?, ?>> stats;


    private MMOItemType(String id, String name, ModifierSource modifierSource, boolean weapon, String loreFormat, ItemStack item, List<ItemStat<?, ?>> stats) {
        this.id = id;
        this.name = name;
        this.modifierSource = modifierSource;
        this.weapon = weapon;
        this.loreFormat = loreFormat;
        this.item = item;
        this.unidentifiedTemplate = new UnidentifiedItem(this);
        this.stats = stats;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ModifierSource getModifierSource() {
        return modifierSource;
    }

    public boolean isWeapon() {
        return weapon;
    }

    public String getLoreFormat() {
        return loreFormat;
    }


    public ItemStack getItem() {
        return item;
    }

    public UnidentifiedItem getUnidentifiedTemplate() {
        return unidentifiedTemplate;
    }

    public List<ItemStat<?, ?>> getStats() {
        return stats;
    }

    public boolean isFourGUIMode() {
        return this.modifierSource == ModifierSource.ARMOR;
    }

    public ConfigFile getConfigFile() {
        return new ConfigFile("/item", getId().toLowerCase());
    }

    public static MMOItemType load(@NotNull TypeManager manager, @NotNull ConfigurationSection section) {
        final String id = section.getName();
        final String name = section.getString("name");
        final ModifierSource modifierSource = ModifierSource.valueOf(section.getString("modifier-source"));
        final boolean weapon = section.getBoolean("weapon");
        final String loreFormat = section.getString("lore-format");
        final ItemStack item = read(section.getString("display", Material.STONE.toString()));


        // TODO: Load the stats
        final List<ItemStat<?, ?>> stats = new ArrayList<>();

        MMOItemType type = new MMOItemType(id, name, modifierSource, weapon, loreFormat, item, stats);
        type.getUnidentifiedTemplate().update(section.getConfigurationSection("unident-item"));
        return type;
    }

    public static ItemStack read(String str) {
        Validate.notNull(str, "Input must not be null");

        String[] split = str.split(":");
        Material material = Material.valueOf(split[0]);
        return split.length > 1 ? MythicLib.plugin.getVersion().getWrapper().textureItem(material, Integer.parseInt(split[1])) : new ItemStack(material);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MMOItemType type = (MMOItemType) o;
        return id.equals(type.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MMOItemType{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", modifierSource=" + modifierSource +
                ", weapon=" + weapon +
                ", loreFormat='" + loreFormat + '\'' +
                '}';
    }

    /**
     * Reads an ItemStack in hopes for finding its MMOItem Type.
     *
     * @param item The item to retrieve the type from
     * @return The type of the item, if it has a type.
     */
    @Nullable
    @Contract("null -> null")
    public static MMOItemType get(@Nullable ItemStack item) {
        if (item == null) return null;
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
    public static MMOItemType get(@Nullable String id) {
        if (id == null) return null;
        String format = id.toUpperCase().replace("-", "_").replace(" ", "_");
        return MMOItems.plugin.getTypes().has(format) ? MMOItems.plugin.getTypes().get(format) : null;
    }

    /**
     * Used in command executors and completions for easier manipulation
     *
     * @param id The type id
     * @return If a registered type with this ID could be found
     */
    public static boolean isValid(@Nullable String id) {
        return id != null && MMOItems.plugin.getTypes().has(id.toUpperCase().replace("-", "_").replace(" ", "_"));
    }
}
