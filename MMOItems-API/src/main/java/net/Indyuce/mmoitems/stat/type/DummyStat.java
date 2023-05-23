package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.stat.StatsCategory;
import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;

/**
 * mmoitems
 * 22/05/2023
 *
 * @author Roch Blondiaux (Kiwix).
 *
 * This is a dummy stat, used to create a new category of stats.
 * This is mainly used to create the {@link net.Indyuce.mmoitems.stat.ItemStatCategory}
 */
@ApiStatus.Internal
public class DummyStat extends DoubleStat implements InternalStat {

    public DummyStat(StatsCategory category) {
        super(category.id(), category, Material.BARRIER, category.fancyName(), new String[0]);
    }

}
