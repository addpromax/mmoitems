package net.Indyuce.mmoitems.api.item.type;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.MythicLibSkillHandler;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.item.util.identify.UnidentifiedItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * mmoitems
 * 24/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class MMOItemType {

    private final String id;
    private final String name;
    private final Type type;
    private final ModifierSource modifierSource;
    private final boolean weapon;
    private final String loreFormat;
    private final ItemStack item;
    private final UnidentifiedItem unidentifiedTemplate;
    private final Map<ItemStat<?, ?>, Double> stats;
    private final Map<TriggerType, Script> scripts;


    protected MMOItemType(String id, String name, Type type, ModifierSource modifierSource, boolean weapon, String loreFormat, ItemStack item, Map<TriggerType, Script> scripts, Map<ItemStat<?, ?>, Double> stats) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.modifierSource = modifierSource;
        this.weapon = weapon;
        this.loreFormat = loreFormat;
        this.item = item;
        this.unidentifiedTemplate = new UnidentifiedItem(this);
        this.scripts = scripts;
        this.stats = stats;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ModifierSource getModifierSource() {
        return modifierSource;
    }

    public boolean isWeapon() {
        return weapon;
    }

    public String getLoreFormat() {
        return loreFormat;
    }

    public Type getType() {
        return type;
    }

    public ItemStack getItem() {
        return item;
    }

    public UnidentifiedItem getUnidentifiedTemplate() {
        return unidentifiedTemplate;
    }

    public boolean hasStat(ItemStat<?, ?> stat) {
        return stats.containsKey(stat);
    }

    public double getStat(ItemStat<?, ?> stat) {
        return stats.getOrDefault(stat, 0.0);
    }

    public Map<ItemStat<?, ?>, Double> getStats() {
        return stats;
    }

    public boolean isFourGUIMode() {
        return this.modifierSource == ModifierSource.ARMOR;
    }

    public ConfigFile getConfigFile() {
        return new ConfigFile("/item", getId().toLowerCase());
    }

    public Map<TriggerType, Script> getScripts() {
        return this.scripts;
    }

    public boolean hasScript(TriggerType type) {
        return this.scripts.containsKey(type);
    }

    public Optional<Script> getScript(TriggerType type) {
        return Optional.ofNullable(this.scripts.get(type));
    }

    public @Nullable SkillResult applyScript(TriggerType type, AttackMetadata attackMetadata, PlayerData playerData, LivingEntity target, Weapon weapon) {
        return getScript(type)
                .map(script -> new SimpleSkill(type, new MythicLibSkillHandler(script)))
                .map(simpleSkill -> simpleSkill.cast(new SkillMetadata(simpleSkill, attackMetadata, playerData.getPlayer().getLocation(), target.getLocation(), target)))
                .orElse(null);
    }

    public static ItemStack read(String str) {
        Validate.notNull(str, "Input must not be null");

        String[] split = str.split(":");
        Material material = Material.valueOf(split[0]);
        return split.length > 1 ? MythicLib.plugin.getVersion().getWrapper().textureItem(material, Integer.parseInt(split[1])) : new ItemStack(material);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MMOItemType type = (MMOItemType) o;
        return id.equals(type.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MMOItemType{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", modifierSource=" + modifierSource +
                ", weapon=" + weapon +
                ", loreFormat='" + loreFormat + '\'' +
                ", type=" + type +
                '}';
    }

    /**
     * Reads an ItemStack in hopes for finding its MMOItem Type.
     *
     * @param item The item to retrieve the type from
     * @return The type of the item, if it has a type.
     */
    @Nullable
    @Contract("null -> null")
    public static MMOItemType get(@Nullable ItemStack item) {
        if (item == null) return null;
        return get(NBTItem.get(item).getType());
    }

    /**
     * Used in command executors and completions for easier manipulation
     *
     * @param id The type id
     * @return The type or null if it couldn't be found
     */
    @Nullable
    @Contract("null -> null")
    public static MMOItemType get(@Nullable String id) {
        if (id == null) return null;
        String format = id.toUpperCase().replace("-", "_").replace(" ", "_");
        return MMOItems.plugin.getTypes().has(format) ? MMOItems.plugin.getTypes().get(format) : null;
    }

    /**
     * Used in command executors and completions for easier manipulation
     *
     * @param id The type id
     * @return If a registered type with this ID could be found
     */
    public static boolean isValid(@Nullable String id) {
        return id != null && MMOItems.plugin.getTypes().has(id.toUpperCase().replace("-", "_").replace(" ", "_"));
    }

    public static MMOItemType load(@NotNull ConfigurationSection section) {
        final String id = section.getName();
        final String name = section.getString("name");
        final ModifierSource modifierSource = ModifierSource.valueOf(section.getString("modifier-source"));
        final boolean weapon = section.getBoolean("weapon");
        final String loreFormat = section.getString("lore-format");
        final ItemStack item = read(section.getString("display", Material.STONE.toString()));
        final Type superType = Arrays.stream(Type.values())
                .filter(type1 -> Objects.equals(section.getString("type").toLowerCase(), type1.name().toLowerCase()))
                .findFirst()
                .orElse(Type.NONE);

        // Scripts
        final Map<TriggerType, Script> scripts = new HashMap<>();
        ConfigurationSection scriptSection = section.getConfigurationSection("scripts");
        if (scriptSection != null)
            scriptSection.getKeys(true)
                    .forEach(key -> TriggerType.values()
                            .stream()
                            .filter(triggerType -> triggerType.name().equalsIgnoreCase(key))
                            .filter(type1 -> scriptSection.isString(key))
                            .findFirst()
                            .ifPresent(triggerType -> scripts.put(triggerType, MythicLib.plugin.getSkills().getScriptOrThrow(scriptSection.getString(key)))));


        // Stats
        final Map<ItemStat<?, ?>, Double> stats = new HashMap<>();
        ConfigurationSection statSection = section.getConfigurationSection("stats");
        if (statSection != null)
            statSection.getKeys(true)
                    .forEach(key -> {
                        String format = key.toUpperCase().replace("-", "_").replace(" ", "_");
                        ItemStat<?, ?> stat = MMOItems.plugin.getStats().get(format);
                        Validate.notNull(stat, String.format("Could not find stat called '%s'", format));
                        if (!statSection.isDouble(key))
                            throw new IllegalArgumentException(String.format("Stat value must be a double (%s) in item-types.yml", format));
                        stats.put(stat, statSection.getDouble(key));
                    });

        MMOItemType type = new MMOItemType(id, name, superType, modifierSource, weapon, loreFormat, item, scripts, stats);
        type.getUnidentifiedTemplate().update(section.getConfigurationSection("unident-item"));
        return type;
    }

    public enum Type {
        GEM_STONE,
        RANGE,
        BLOCK,
        CONSUMABLE,
        NONE
    }
}
