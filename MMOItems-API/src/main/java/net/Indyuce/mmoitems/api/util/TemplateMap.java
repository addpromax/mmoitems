package net.Indyuce.mmoitems.api.util;

import net.Indyuce.mmoitems.api.item.type.MMOItemType;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Allows the use of two nested maps to efficiently store data about mmoitem
 * templates. The first nested map is for the item type, the second is for the
 * item ID.
 *
 * @param <C> The class of the value you want to assign to every mmoitem
 *            template
 * @author cympe
 */
public class TemplateMap<C> {

    private final Map<MMOItemType, Map<String, C>> typeMap = new HashMap<>();

    /**
     * @param type The item type
     * @param id   The template identifier
     * @return If a template has some value stored in that map
     */
    @Contract("null, _ -> false; _, null -> false")
    public boolean hasValue(@Nullable MMOItemType type, @Nullable String id) {
        if (type == null || id == null)
            return false;
        return typeMap.containsKey(type) && typeMap.getOrDefault(type, new HashMap<>()).containsKey(id);
    }

    /**
     * @param type The item type
     * @param id   The template identifier
     * @return Returns the value stored in the template map
     */
    @Nullable
    @Contract("null, _ -> null; _, null -> null")
    public C getValue(@Nullable MMOItemType type, @Nullable String id) {
        if (type == null || id == null) return null;
        return typeMap.getOrDefault(type, new HashMap<>()).get(id);
    }

    /**
     * Unregisters a value from the map
     *
     * @param type The item type
     * @param id   The template identifier
     */
    public void removeValue(@Nullable MMOItemType type, @Nullable String id) {
        if (type == null || id == null) return;
        typeMap.getOrDefault(type, new HashMap<>()).remove(id);
    }

    /**
     * Registers a value for a specific mmoitem template
     *
     * @param type  The item type
     * @param id    The template identifier
     * @param value The value to registered
     */
    public void setValue(@NotNull MMOItemType type, @NotNull String id, @NotNull C value) {
        Validate.notNull(value, "Value cannot be null");

        this.typeMap.computeIfAbsent(type, k -> new HashMap<>()).put(id, value);
    }

    /**
     * Applies a specific consumer for every template. This is used to postload
     * all templates when MMOItems enables
     *
     * @param action Action performed for every registered template
     */
    public void forEach(@NotNull Consumer<C> action) {
        typeMap.values().forEach(stringCMap -> stringCMap.values().forEach(action));
    }

    /**
     * @return Collects all the values registered in this template map.
     */
    @NotNull
    public Collection<C> collectValues() {
        Set<C> collected = new HashSet<>();
        typeMap.values().forEach(submap -> collected.addAll(submap.values()));
        return collected;
    }

    /**
     * @param type The item type
     * @return Collects all the values registered in this template map with a
     * specific item type
     */
    @NotNull
    public Collection<C> collectValues(@NotNull MMOItemType type) {
        return typeMap.containsKey(type) ? typeMap.get(type).values() : new HashSet<>();
    }

    /**
     * Clears the map
     */
    public void clear() {
        typeMap.clear();
    }
}
