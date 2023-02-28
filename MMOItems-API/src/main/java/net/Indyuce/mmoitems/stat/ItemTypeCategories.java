package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * mmoitems
 * 28/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class ItemTypeCategories extends StringListStat {

    public ItemTypeCategories() {
        super("CATEGORIES", VersionMaterial.WRITABLE_BOOK.toMaterial(), "Categories", new String[]{"The item type categories."}, new String[]{"all"});
    }

    @Override
    @SuppressWarnings("unchecked")
    public StringListData whenInitialized(Object object) {
        Validate.isTrue(object instanceof List<?>, "Must specify a string list");
        return new StringListData((List<String>) object);
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringListData data) {
        item.addItemTag(getAppliedNBT(data));
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new StatEdition(inv, ItemStats.LORE).enable("Write in the chat the lore line you want to add.");

        if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().contains("categories")) {
            List<String> categories = inv.getEditedSection().getStringList("categories");
            if (categories.isEmpty())
                return;

            String last = categories.get(categories.size() - 1);
            categories.remove(last);
            inv.getEditedSection().set("categories", categories.isEmpty() ? null : categories);
            inv.registerTemplateEdition();
            inv.getPlayer()
                    .sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed '" + MythicLib.plugin.parseColors(last) + ChatColor.GRAY + "'.");
        }
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        List<String> categories = inv.getEditedSection().contains("categories") ? inv.getEditedSection().getStringList("categories") : new ArrayList<>();
        categories.add(message);
        inv.getEditedSection().set("categories", categories);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Category successfully added.");
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<StringListData> statData) {
        statData.ifPresentOrElse(stringListData -> {
            lore.add(ChatColor.GRAY + "Current Value:");
            lore.addAll(MythicLib.plugin.parseColors(stringListData.getList()
                    .stream()
                    .map(s -> ChatColor.GRAY + s)
                    .toList()));
        }, () -> lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None"));

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a category.");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last category.");
    }
}
