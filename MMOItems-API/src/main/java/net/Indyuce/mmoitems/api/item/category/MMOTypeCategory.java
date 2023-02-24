package net.Indyuce.mmoitems.api.item.category;

import net.Indyuce.mmoitems.api.item.type.MMOItemType;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * mmoitems
 * 24/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class MMOTypeCategory {

    private final String id;
    private final String name;
    private final ItemStack item;
    private final String loreFormat;
    private final List<ItemStat<?, ?>> stats;

    public MMOTypeCategory(String id, String name, ItemStack item, String loreFormat, List<ItemStat<?, ?>> stats) {
        this.id = id;
        this.name = name;
        this.item = item;
        this.loreFormat = loreFormat;
        this.stats = stats;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getLoreFormat() {
        return loreFormat;
    }

    public List<ItemStat<?, ?>> getStats() {
        return stats;
    }

    public static MMOTypeCategory load(@NotNull ConfigurationSection section) {
        final String id = section.getName();
        final String name = section.getString("name");
        final ItemStack item = MMOItemType.read(section.getString("display", Material.STONE.toString()));
        final String loreFormat = section.getString("lore-format");
        final List<ItemStat<?, ?>> stats = new ArrayList<>();

        return new MMOTypeCategory(id, name, item, loreFormat, stats);
    }
}
