package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.element.Element;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.util.ElementStatType;
import net.Indyuce.mmoitems.util.PluginUtils;

/**
 * mmoitems
 * 24/05/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class ElementStat extends DoubleStat {

    private final Element element;
    private final ElementStatType type;

    public ElementStat(ElementStatType type, Element element) {
        super(String.format("ELEMENTS_%s_%s", type.name(), element.getName()),
                StatsCategory.ELEMENTS,
                element.getIcon(),
                PluginUtils.capitalizeAllWorlds(String.format("%s %s", element.getName(), type.getName())),
                new String[]{},
                new String[]{"slashing", "piercing", "blunt", "catalyst", "range", "tool", "armor", "gem_stone"}
        );
        this.element = element;
        this.type = type;
    }

    public Element getElement() {
        return element;
    }

    public ElementStatType getType() {
        return type;
    }



}
