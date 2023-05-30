package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.CategoryEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.Previewable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * mmoitems
 * 22/05/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class ItemStatCategory extends ItemStat<RandomStatData<StringData>, StringData> implements Previewable<RandomStatData<StringData>, StringData> {

    private final StatsCategory targetCategory;
    public ItemStatCategory(@NotNull StatsCategory category, @NotNull Material material, String... lore) {
        super(String.format("CATEGORY_%s", category.name()), StatsCategory.NONE, material, category.fancyName(), lore, new String[]{"all"});
        this.targetCategory = category;
    }

    @Override
    public RandomStatData<StringData> whenInitialized(Object object) {
        return null;
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {

    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StringData data) {
        return new ArrayList<>();
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new CategoryEdition(inv.getPlayer(), inv.getEdited(), this.targetCategory).open(inv.getPage());
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {

    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

    }

    @Nullable
    @Override
    public StringData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        return null;
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<RandomStatData<StringData>> statData) {
        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to access the category edition menu.");
    }

    @NotNull
    @Override
    public StringData getClearStatData() {
        return new StringData(StatsCategory.NONE.id());
    }

    @Override
    public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull StringData currentData, @NotNull RandomStatData<StringData> templateData) throws IllegalArgumentException {

    }
}
