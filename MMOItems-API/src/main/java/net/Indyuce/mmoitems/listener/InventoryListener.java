package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.api.event.armorequip.ArmorEquipEvent;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.jetbrains.annotations.NotNull;

/**
 * mmoitems
 * 24/01/2023
 *
 * @author Roch Blondiaux (Kiwix).
 * <p>
 * All listeners use MONITOR priority to avoid conflicts with other plugins.
 * So basically, they'll only trigger an update after the event has been processed
 * by all other listeners.
 */
public class InventoryListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        triggerUpdate(e.getPlayer());
        MMOItems.log("Player " + e.getPlayer().getName() + " joined the server. Inventory updated.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrop(PlayerDropItemEvent e) {
        if (!e.isCancelled())
            triggerUpdate(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHandSwap(PlayerSwapHandItemsEvent e) {
        if (!e.isCancelled())
            triggerUpdate(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSlotChange(PlayerItemHeldEvent e) {
        if (!e.isCancelled())
            triggerUpdate(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrag(InventoryDragEvent e) {
        if (!e.isCancelled())
            triggerUpdate((Player) e.getView().getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemMove(InventoryMoveItemEvent e) {
        if (!e.isCancelled() && e.getDestination().getHolder() instanceof Player)
            triggerUpdate((Player) e.getDestination().getHolder());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorEquip(ArmorEquipEvent e) {
        if (!e.isCancelled())
            triggerUpdate(e.getPlayer());
    }

    private void triggerUpdate(@NotNull Player player) {
        PlayerData.get(player).getInventory().update();
    }
}
