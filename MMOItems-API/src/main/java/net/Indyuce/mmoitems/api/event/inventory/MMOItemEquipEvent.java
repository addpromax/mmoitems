package net.Indyuce.mmoitems.api.event.inventory;

import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MMOItemEquipEvent extends Event {

    private final int oldHashcode;
    private final int newHashcode;
    private final @Nullable EquippedItem oldItem;
    private final @Nullable EquippedItem newItem;

    public MMOItemEquipEvent(int oldHashcode, int newHashcode, @Nullable EquippedItem oldItem, @Nullable EquippedItem newItem) {
        this.oldHashcode = oldHashcode;
        this.newHashcode = newHashcode;
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    public int getOldHashcode() {
        return oldHashcode;
    }

    public int getNewHashcode() {
        return newHashcode;
    }

    public @Nullable EquippedItem getOldItem() {
        return oldItem;
    }

    public @Nullable EquippedItem getNewItem() {
        return newItem;
    }

    @NotNull static final HandlerList handlers = new HandlerList();
    @NotNull public HandlerList getHandlers() { return handlers; }
    @NotNull public static HandlerList getHandlerList() { return handlers; }
}
