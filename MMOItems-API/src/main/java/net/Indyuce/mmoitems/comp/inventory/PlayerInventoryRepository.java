package net.Indyuce.mmoitems.comp.inventory;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import net.Indyuce.mmoitems.comp.inventory.model.PlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * mmoitems
 * 17/01/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class PlayerInventoryRepository {

    private final List<PlayerInventory> inventories = new ArrayList<>();

    /**
     * Use this to tell MMOItems to search equipment here ---
     * Items that will give their stats to the player.
     * <p></p>
     * Note that if the items are not held in the correct slot (OFF_CATALYST not held in OFFHAND)
     * they won't provide their stats. You may fool MMOItems by telling the <code>EquippedItem</code> that it is
     * in their correct slot though, but that is up to you.
     * <p></p>
     * <b>Calling twice will cause duplicates</b> but is nevertheless allowed if you really want to.
     */
    public void register(@NotNull PlayerInventory pInventory) {
        inventories.add(pInventory);
        if (pInventory instanceof Listener)
            Bukkit.getPluginManager().registerEvents((Listener) pInventory, MMOItems.plugin);
    }

    public void unregisterIf(Predicate<PlayerInventory> filter) {
        inventories.removeIf(playerInventory -> {
            if (filter.test(playerInventory)) {
                if (playerInventory instanceof Listener)
                    HandlerList.unregisterAll((Listener) playerInventory);
                return true;
            }
            return false;
        });
    }

    /**
     * Can be used by external plugins to clear current inventory
     * handlers if you want offhand and mainhand items removed
     * from the player inventory
     */
    public void unregisterAll() {
        inventories.stream()
                .filter(playerInventory -> playerInventory instanceof Listener)
                .forEach(playerInventory -> HandlerList.unregisterAll((Listener) playerInventory));
        inventories.clear();
    }

    /**
     * @return A copy of the list of registered inventories.
     */
    public ArrayList<PlayerInventory> all() {
        return new ArrayList<>(inventories);
    }

    /**
     * @return Gets the totality of items from all the PlayerInventories ie all the items that will add their stats to the player.
     */
    @NotNull
    public List<EquippedItem> inventory(@NotNull Player player) {
        ArrayList<EquippedItem> cumulative = new ArrayList<>();
        for (PlayerInventory inv : inventories)
            cumulative.addAll(inv.getInventory(player));
        return cumulative;
    }
}
