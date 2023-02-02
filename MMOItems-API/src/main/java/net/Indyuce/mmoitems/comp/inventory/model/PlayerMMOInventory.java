package net.Indyuce.mmoitems.comp.inventory.model;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import net.Indyuce.mmoitems.comp.inventory.PlayerInventoryHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * mmoitems
 * 17/01/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class PlayerMMOInventory {

    private final UUID uniqueId;
    private final List<EquippedItem> content = new ArrayList<>();
    private final PlayerInventoryHandler handler;

    public PlayerMMOInventory(@NotNull PlayerData data) {
        this.uniqueId = data.getUniqueId();
        this.handler = new PlayerInventoryHandler(data, this);
    }

    public void addItem(@NotNull EquippedItem item) {
        content.add(item);
    }

    public void remove(@NotNull EquippedItem item) {
        content.remove(item);
    }

    public void remove(int slot) {
        content.removeIf(item -> item instanceof SlotEquippedItem && ((SlotEquippedItem) item).getSlotNumber() == slot);
    }

    public void update() {
        handler.start();
    }

    public void unload() {
        this.content.clear();
        this.handler.reset();
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    /**
     * @deprecated Use {@link PlayerMMOInventory#update()} instead.
     */
    public void scheduleUpdate() {
        this.update();
        MMOItems.plugin.getLogger().warning("PlayerMMOInventory#scheduleUpdate() is deprecated and will be removed in the next version. Use PlayerMMOInventory#update() instead.");
    }

    /* Getters */
    public UUID uniqueId() {
        return uniqueId;
    }

    public List<EquippedItem> equipped() {
        return Collections.unmodifiableList(content);
    }

    public PlayerInventoryHandler getHandler() {
        return handler;
    }
}
