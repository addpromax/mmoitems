package net.Indyuce.mmoitems.manager;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.type.MMOItemType;
import net.Indyuce.mmoitems.manager.ConfigManager.DefaultFile;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TypeManager implements Reloadable {

    private final List<MMOItemType> types = new ArrayList<>();

    /**
     * Reloads the type manager. It entirely empties the currently registered
     * item types, registers default item types again and reads item-types.yml
     */
    public void reload() {
        this.types.clear();

        /*
         * Register all other types. Important: check if the map already
         * contains the id, this way the DEFAULT types are not registered twice,
         * and only custom types are registered with a parent.
         */
        DefaultFile.ITEM_TYPES.checkFile();

        // Types
        ConfigFile typesConfig = new ConfigFile("item-types.yml");
        ConfigurationSection typesSection = typesConfig.getConfig();
        typesSection.getKeys(false)
                .stream()
                .filter(typesSection::isConfigurationSection)
                .map(typesSection::getConfigurationSection)
                .filter(Objects::nonNull)
                .map(MMOItemType::load)
                .peek(this.types::add)
                .forEach(mmoItemType -> mmoItemType.getStats().clear());

        // DEBUG
        MMOItems.log(String.format("Loaded %d item types.", this.types.size()));
    }

    public void register(MMOItemType type) {
        this.types.add(type);
    }

    public void registerAll(MMOItemType... types) {
        for (MMOItemType type : types)
            register(type);
    }

    /**
     * @param id Internal ID of the type
     * @return The MMOItem Type if it found.
     */
    @Contract("null -> null")
    @Nullable
    public MMOItemType get(@Nullable String id) {
        if (id == null) return null;
        return this.types.stream()
                .filter(mmoItemType -> mmoItemType.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Contract("null -> fail")
    @NotNull
    public MMOItemType getOrThrow(@Nullable String id) {
        if (id == null) throw new IllegalArgumentException(String.format("No type found with id '%s'", id));
        return this.types.stream()
                .filter(mmoItemType -> mmoItemType.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("No type found with id '%s'", id)));
    }

    public boolean has(String id) {
        return this.types.stream()
                .anyMatch(mmoItemType -> mmoItemType.getId().equals(id));
    }

    public List<MMOItemType> getAll() {
        return this.types;
    }

    /**
     * @return The names of all loaded types.
     */
    public List<String> getAllTypeNames() {
        return this.types.stream()
                .map(MMOItemType::getName)
                .collect(Collectors.toList());
    }
}
