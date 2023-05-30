package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.StatsCategory;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * mmoitems
 * 23/05/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class CategoryEdition extends EditionInventory {

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private static final int[] FILLER_SLOTS = {0, 1, 3, 5, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
    private static final NamespacedKey PAGINATION_KEY = NamespacedKey.fromString("mmoitems:category_edition_pagination");
    private static final NamespacedKey STAT_KEY = NamespacedKey.fromString("mmoitems:category_edition_stat");


    private final StatsCategory category;
    private final List<ItemStat<?, ?>> stats = new ArrayList<>();
    private final int maxPage;
    private final int pageSize;
    private int page;

    public CategoryEdition(@NotNull Player player, @NotNull MMOItemTemplate template, StatsCategory category) {
        super(player, template);
        this.category = category;

        this.stats.addAll(this.category.stats());
        this.pageSize = SLOTS.length;
        this.maxPage = 1 + this.stats.size() / this.pageSize;
        this.page = 1;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 54, String.format("Category Edition | %s", this.category.fancyName()));

        // Fillers
        this.addFillers(inv);

        // Pagination
        if (this.page > 1) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevPageMeta = prevPage.getItemMeta();
            prevPageMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
            prevPageMeta.getPersistentDataContainer().set(PAGINATION_KEY, PersistentDataType.INTEGER, this.page - 1);
            prevPage.setItemMeta(prevPageMeta);
            inv.setItem(45, prevPage);
        }
        if (this.page < this.maxPage) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.setDisplayName(ChatColor.GREEN + "Next Page");
            nextPageMeta.getPersistentDataContainer().set(PAGINATION_KEY, PersistentDataType.INTEGER, this.page + 1);
            nextPage.setItemMeta(nextPageMeta);
            inv.setItem(53, nextPage);
        }

        // Stats
        final int startingIndex = (page - 1) * this.pageSize;
        int lastSlot = 0;
        for (int i = 0; i < this.pageSize; i++) {
            final int index = startingIndex + i;
            if (index >= this.stats.size())
                break;

            ItemStat stat = this.stats.get(index);

            ItemStack statItem = new ItemStack(stat.getDisplayMaterial());
            ItemMeta statMeta = statItem.getItemMeta();
            statMeta.setDisplayName(ChatColor.GREEN + stat.getName());

            // Lore
            List<String> statLore = MythicLib.plugin.parseColors(Arrays.stream(stat.getLore()).map(s -> ChatColor.GRAY + s).collect(Collectors.toList()));
            statLore.add("");
            stat.whenDisplayed(statLore, this.getEventualStatData(stat));

            // Data
            statMeta.getPersistentDataContainer().set(STAT_KEY, PersistentDataType.STRING, stat.getId());

            statMeta.setLore(statLore);
            statItem.setItemMeta(statMeta);

            inv.setItem(SLOTS[i], statItem);
            lastSlot = i;
        }

        // Blank slots
        this.fillBlankSlots(inv, lastSlot);

        // Edition controls
        this.addEditionInventoryItems(inv, true);

        return inv;
    }

    @Override
    public void whenClicked(InventoryClickEvent event) {
        event.setCancelled(true);

        final ItemStack item = event.getCurrentItem();
        if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
            return;
        final PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

        // Pagination
        if (container.has(PAGINATION_KEY, PersistentDataType.INTEGER)) {
            this.page = container.getOrDefault(PAGINATION_KEY, PersistentDataType.INTEGER, 1);
            open();
            return;
        }

        // Stats
        if (container.has(STAT_KEY, PersistentDataType.STRING)) {
            final String statId = container.get(STAT_KEY, PersistentDataType.STRING);
            final ItemStat<?, ?> stat = this.stats.stream()
                    .filter(s -> s.getId().equals(statId))
                    .findFirst()
                    .orElse(null);

            // Should not happen but just in case
            if (stat == null)
                return;

            // Check for OP stats
            if (MMOItems.plugin.hasPermissions()
                    && MMOItems.plugin.getLanguage().opStatsEnabled
                    && MMOItems.plugin.getLanguage().opStats.contains(stat.getId())
                    && !MMOItems.plugin.getVault().getPermissions().has((Player) event.getWhoClicked(), "mmoitems.edit.op")) {
                event.getWhoClicked().sendMessage(ChatColor.RED + "You are lacking permission to edit this stat.");
                return;
            }

            stat.whenClicked(this, event);
        }

    }

    private void addFillers(Inventory inv) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        Arrays.stream(FILLER_SLOTS).forEach(slot -> inv.setItem(slot, filler));
    }

    private void fillBlankSlots(Inventory inv, int slotId) {
        ItemStack filler = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = slotId; i < SLOTS.length; i++)
            inv.setItem(SLOTS[i], filler);
    }
}
