package net.Indyuce.mmoitems.comp.inventory;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * mmoitems
 * 17/01/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class PlayerInventoryUpdater extends BukkitRunnable {

    private final PlayerData data;
    private final Map<EquipmentSlot, Integer> lastHashCodes = new HashMap<>();

    public PlayerInventoryUpdater(PlayerData data) {
        this.data = data;
    }

    @Override
    public void run() {
        // Current equipment
        MMOItems.plugin.getInventory().inventory(data.getPlayer())
                .stream()
                .filter(this::needsUpdate)
                .forEach(equippedItem -> {
                    Bukkit.broadcastMessage("Updating " + equippedItem.getNBT().getItem().getType());
                });
    }

    private boolean needsUpdate(@Nullable EquippedItem item) {
        if (isEmpty(item))
            return true;
        int hash = item.getNBT().hashCode();
        boolean result = lastHashCodes.getOrDefault(item.getSlot(), 0) != hash;
        lastHashCodes.put(item.getSlot(), hash);
        return result;
    }

    private boolean isEmpty(@Nullable EquippedItem item) {
        return item == null || item.getNBT().getItem().getType().isAir();
    }
}
