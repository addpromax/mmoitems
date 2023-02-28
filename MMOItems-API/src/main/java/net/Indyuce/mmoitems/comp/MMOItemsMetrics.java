package net.Indyuce.mmoitems.comp;

import io.lumine.mythic.lib.metrics.bukkit.Metrics;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.type.MMOItemType;

public class MMOItemsMetrics extends Metrics {
    public MMOItemsMetrics() {
        super(MMOItems.plugin);

        addCustomChart(new Metrics.SingleLineChart("items", () -> MMOItems.plugin.getTypes().getAll()
                .stream()
                .map(MMOItemType::getConfigFile)
                .map(configFile -> configFile.getConfig().getKeys(false).size())
                .reduce(0, Integer::sum)));
    }
}
