package net.Indyuce.mmoitems.comp.inventory.model;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * mmoitems
 * 17/01/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class PlayerMMOInventory {

    private final UUID uniqueId;
    private final Map<EquippedItem, Integer> content = new HashMap<>();

    public PlayerMMOInventory(@NotNull PlayerData data) {
        this.uniqueId = data.getUniqueId();
    }

    public void addItem(@NotNull EquippedItem item) {
        content.put(item, item.hashCode());
    }

    public void remove(@NotNull EquippedItem item) {
        content.remove(item);
    }

    public void remove(@NotNull EquipmentSlot slot) {
        content.keySet().removeIf((item) -> item.getSlot() == slot);
    }

    /* Getters */
    public UUID uniqueId() {
        return uniqueId;
    }

    public List<EquippedItem> equipped() {
        return Collections.unmodifiableList(new ArrayList<>(content.keySet()));
    }

    public List<Integer> hashCodes() {
        return Collections.unmodifiableList(new ArrayList<>(content.values()));
    }

    public int hashCode(EquipmentSlot slot) {
        return content.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getSlot() == slot)
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(0);
    }


    public Map<EquippedItem, Integer> content() {
        return content;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, content.keySet());
    }
}
