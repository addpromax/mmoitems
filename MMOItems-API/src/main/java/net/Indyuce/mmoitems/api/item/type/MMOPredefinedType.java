package net.Indyuce.mmoitems.api.item.type;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.TypeSet;
import net.Indyuce.mmoitems.api.item.type.set.MMOTypeSet;
import net.Indyuce.mmoitems.api.item.util.identify.UnidentifiedItem;
import net.Indyuce.mmoitems.manager.TypeManager;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * mmoitems
 * 20/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class MMOPredefinedType implements MMOItemType {

    private final String id;
    private final MMOTypeSet set;
    private final ModifierSource modifierSource;
    private final boolean weapon;

    // Configurable
    private String name;

    @Nullable
    private String loreFormat;

    /**
     * Used to display the item in the item explorer and in the item recipes
     * list in the advanced workbench. can also be edited using the config
     * files.
     */
    private ItemStack item;
    private UnidentifiedItem unidentifiedTemplate;

    /**
     * List of stats which can be applied onto an item which has this type. This
     * improves performance when generating an item by a significant amount.
     */
    private final List<ItemStat<?, ?>> stats = new ArrayList<>();

    public MMOPredefinedType(String id, boolean weapon, ModifierSource modSource) {
        this.id = id.toUpperCase().replace("-", "_").replace(" ", "_");
        this.modifierSource = modSource;
        this.weapon = weapon;
        this.loreFormat = null;
    }

    public MMOPredefinedType(@NotNull TypeManager manager, @NotNull ConfigurationSection config) {
        id = config.getName().toUpperCase().replace("-", "_").replace(" ", "_");

        parent = manager.get(config.getString("parent", "").toUpperCase().replace("-", "_").replace(" ", "_"));

        set = (parent != null ? parent.set : TypeSet.EXTRA);
        weapon = (parent != null && parent.weapon);
        modifierSource = (parent != null ? parent.modifierSource : ModifierSource.OTHER);
        this.loreFormat = config.getString("LoreFormat", (parent != null ? parent.loreFormat : null));
    }

    @NotNull
    @Override
    public String getId() {
        return this.id;
    }

    @NotNull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isWeapon() {
        return this.weapon;
    }

    @Override
    public ModifierSource getModifierSource() {
        return this.modifierSource;
    }

    @Override
    public ItemStack toItemStack() {
        return this.item.clone();
    }

    @Override
    public boolean isFourGUIMode() {

    }

    @Override
    public String getLoreFormat() {
        return this.loreFormat;
    }

    @Override
    public List<ItemStat<?, ?>> getAvailableStats() {
        return this.stats;
    }

    @Override
    public ConfigFile getConfiguration() {
        return new ConfigFile("/item", this.id.toLowerCase());
    }

    @Override
    public UnidentifiedItem getUnidentifiedItem() {
        return this.unidentifiedTemplate;
    }

    @Override
    public void load(ConfigurationSection config) {
        Validate.notNull(config, String.format("Could not find config for %s", getId()));

        name = config.getString("name", name);
        item = read(config.getString("display", item == null ? Material.STONE.toString() : item.getType().toString()));

        (unidentifiedTemplate = new UnidentifiedItem(this)).update(config.getConfigurationSection("unident-item"));

        // Getting overridden?
        loreFormat = config.getString("LoreFormat", (parent != null ? parent.loreFormat : loreFormat));
    }

    private ItemStack read(String data) {
        Validate.notNull(data, "Input must not be null");

        String[] split = data.split(":");
        Material material = Material.valueOf(split[0]);
        return split.length > 1 ? MythicLib.plugin.getVersion().getWrapper().textureItem(material, Integer.parseInt(split[1])) : new ItemStack(material);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MMOTypeSet type = (MMOTypeSet) o;
        return id.equals(type.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Type{" +
                "id='" + id + '\'' +
                '}';
    }
}
