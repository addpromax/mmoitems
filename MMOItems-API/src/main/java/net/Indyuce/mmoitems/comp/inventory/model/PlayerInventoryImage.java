package net.Indyuce.mmoitems.comp.inventory.model;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * mmoitems
 * 24/01/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class PlayerInventoryImage {

    private final PlayerData data;
    private final List<SlotEquippedItem> equipped;
    private final Map<Integer, VolatileMMOItem> cache;
    private final Map<Integer, Integer> hashCodes;
    private final long timestamp;

    public PlayerInventoryImage(@NotNull PlayerData data) {
        this.data = data;
        this.equipped = new ArrayList<>();
        this.hashCodes = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
        this.cache = new HashMap<>();
    }

    public @NotNull PlayerData data() {
        return data;
    }

    public @NotNull List<SlotEquippedItem> equipped() {
        return equipped;
    }

    public @NotNull Map<Integer, Integer> hashCodes() {
        return hashCodes;
    }

    public long timestamp() {
        return timestamp;
    }

    public @NotNull Optional<SlotEquippedItem> get(int slot) {
        return equipped.stream().filter(e -> e.getSlotNumber() == slot).findFirst();
    }

    public @NotNull Optional<VolatileMMOItem> getCached(int slot) {
        return Optional.ofNullable(cache.get(slot));
    }

    public void cache() {
        this.cache.clear();
        this.equipped.stream()
                .filter(item -> item.getNBT() != null)
                .forEach(e -> this.cache.put(e.getSlotNumber(), new VolatileMMOItem(e.getNBT())));
    }

    public boolean isDifferent(@NotNull PlayerInventoryImage image) {
        return image.equipped.size() != equipped.size() || !hashCodes.equals(image.hashCodes());
    }

    public List<SlotEquippedItem> difference(@NotNull PlayerInventoryImage image) {
        if (!isDifferent(image)) return Collections.emptyList();
        return hashCodes.entrySet()
                .stream()
                .filter(e -> !Objects.equals(e.getValue(), image.hashCodes().get(e.getKey())))
                .map(Map.Entry::getKey)
                .map(i -> equipped.stream().filter(e -> e.getSlotNumber() == i).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static @NotNull PlayerInventoryImage make(@NotNull PlayerData data) {
        PlayerInventoryImage image = new PlayerInventoryImage(data);
        MMOItems.plugin.getInventory()
                .inventory(data.getPlayer())
                .stream()
                .filter(Objects::nonNull)
                .filter(i -> i instanceof SlotEquippedItem)
                .map(i -> (SlotEquippedItem) i)
                .forEach(i -> {
                    image.equipped.add(i);
                    image.hashCodes.put(i.getSlotNumber(), isEmpty(i) ? -1 : i.hashCode());
                });
        return image;
    }

    private static boolean isEmpty(@Nullable SlotEquippedItem item) {
        return item == null || item.getItem() == null;
    }

    @Override
    public int hashCode() {
        return equipped.hashCode();
    }
}
