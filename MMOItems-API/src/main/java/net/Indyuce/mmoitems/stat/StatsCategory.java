package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.util.PluginUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * mmoitems
 * 22/05/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public enum StatsCategory {
    MMOCORE,
    ELEMENTS,
    RESTORE,
    NONE;

    public static final String NBT_PATH = "mmoitems-stats";

    public @Nullable String id() {
        if (this == NONE) return null;
        return name().toLowerCase();
    }

    public @Nullable String fancyName() {
        if (this == NONE) return null;
        return PluginUtils.capitalizeAllWorlds(name().toLowerCase().replace("_", " "));
    }

    public @NotNull List<ItemStat<?, ?>> stats() {
        return MMOItems.plugin.getStats()
                .getAll()
                .stream()
                .filter(stat -> stat.getCategory() == this)
                .filter(itemStat -> !(itemStat instanceof InternalStat || itemStat instanceof ItemStatCategory))
                .collect(Collectors.toList());
    }
}
