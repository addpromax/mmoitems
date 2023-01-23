package net.Indyuce.mmoitems.comp.inventory;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemSet;
import net.Indyuce.mmoitems.api.event.inventory.MMOItemEquipEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import net.Indyuce.mmoitems.comp.inventory.model.SlotEquippedItem;
import net.Indyuce.mmoitems.stat.data.*;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * mmoitems
 * 17/01/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class PlayerInventoryUpdater implements Runnable {

    private final PlayerData data;
    private final Map<Integer, Integer> lastHashCodes = new HashMap<>();
    private final Map<Integer, SlotEquippedItem> CACHE = new HashMap<>();
    private final Map<Integer, List<UUID>> MODIFIERS_CACHE = new HashMap<>();

    private boolean running;

    public PlayerInventoryUpdater(PlayerData data) {
        this.data = data;
        this.running = true;
    }

    @Override
    public void run() {
        if (!this.running)
            return;
        if (!this.data.isOnline()) {
            this.stop();
            return;
        }

        // Refresh item by item
        MMOItems.plugin.getInventory()
                .inventory(data.getPlayer())
                .stream()
                .filter(item -> item instanceof SlotEquippedItem)
                .map(item -> (SlotEquippedItem) item)
                .filter(this::needsUpdate)
                .forEach(eItem -> {
                    final int currentHashcode = lastHashCodes.getOrDefault(eItem.getSlotNumber(), -1);
                    final int newHashcode = isEmpty(eItem) ? -1 : eItem.hashCode();
                    final SlotEquippedItem oldItem = CACHE.get(currentHashcode);

                    if (currentHashcode == newHashcode)
                        return;

                    // Call item equip event
                    Bukkit.getPluginManager().callEvent(new MMOItemEquipEvent(currentHashcode, newHashcode, oldItem, eItem));
                    MMOItems.log("Calling item equip event for slot: " + eItem.getSlotNumber() + " with hashcodes: " + currentHashcode + " -> " + newHashcode);

                    this.removeStats(oldItem, currentHashcode);

                    this.addStats(eItem, newHashcode);
                });

        // Refresh item sets
        List<EquippedItem> equippedItems = MMOItems.plugin.getInventory()
                .inventory(data.getPlayer());
        this.recalculateItemSet(equippedItems);

        // Calculate player stats
        this.data.getStats().updateStats();

        // Update stats from external plugins
        MMOItems.plugin.getRPG().refreshStats(this.data);

        // TODO: Call inventory refresh event
        // Bukkit.getPluginManager().callEvent(new MMOInventoryRefreshEvent(inventory.equipped(), getPlayer(), this));
    }

    private void recalculateItemSet(@NotNull List<EquippedItem> equippedItems) {
        final Map<ItemSet, Integer> itemSetCount = new HashMap<>();
        for (EquippedItem equipped : equippedItems) {
            final String tag = equipped.getNBT().getString("MMOITEMS_ITEM_SET");
            final @Nullable ItemSet itemSet = MMOItems.plugin.getSets().get(tag);
            if (itemSet == null)
                continue;
            itemSetCount.put(itemSet, itemSetCount.getOrDefault(itemSet, 0) + 1);
        }

        // Reset and compute item set bonuses
        this.data.resetSetBonuses();
        for (Map.Entry<ItemSet, Integer> equippedSetBonus : itemSetCount.entrySet()) {
            if (this.data.getSetBonuses() == null)
                this.data.setSetBonuses(equippedSetBonus.getKey().getBonuses(equippedSetBonus.getValue()));
            else
                this.data.getSetBonuses().merge(equippedSetBonus.getKey().getBonuses(equippedSetBonus.getValue()));
        }
    }

    /**
     * Remove all stats from the old item
     *
     * @param oldItem The old item
     */
    private void removeStats(@Nullable SlotEquippedItem oldItem, int hashcode) {
        // Remove all old item attributes & effects
        if (oldItem == null || isEmpty(oldItem))
            return;
        MMOItems.log("Removing old item attributes & effects");
        final MMOItem mmoItem = oldItem.getCached().clone();
        this.data.getInventory().remove(oldItem);

        // Potion effects
        if (mmoItem.hasData(ItemStats.PERM_EFFECTS)) {
            ((PotionEffectListData) mmoItem.getData(ItemStats.PERM_EFFECTS))
                    .getEffects()
                    .forEach(effect -> {
                        this.data.getPermanentPotionEffectsMap().remove(effect.getType(), effect.toEffect());
                        MMOItems.log("Pot effect removed: " + effect.getType());
                    });
        }

        // Item particles
        if (mmoItem.hasData(ItemStats.ITEM_PARTICLES)) {
            ParticleData particleData = (ParticleData) mmoItem.getData(ItemStats.ITEM_PARTICLES);

            if (particleData.getType().hasPriority()) {
                if (this.data.getOverridingItemParticles() != null
                        && this.data.getOverridingItemParticles().getParticleData().equals(particleData)) {
                    this.data.getOverridingItemParticles().cancel();
                    this.data.resetOverridingItemParticles();
                    MMOItems.log("Overriding particle removed: " + particleData.getType());
                }
            } else {
                this.data.getItemParticles()
                        .stream()
                        .filter(particleRunnable -> particleRunnable.getParticleData().equals(particleData))
                        .peek(particleRunnable -> MMOItems.log("Particle removed: " + particleRunnable.getParticleData().getType()))
                        .forEach(BukkitRunnable::cancel);
                this.data.getItemParticles().removeIf(BukkitRunnable::isCancelled);
            }
        }

        // Permissions
        if (MMOItems.plugin.hasPermissions() && mmoItem.hasData(ItemStats.GRANTED_PERMISSIONS)) {
            final Permission perms = MMOItems.plugin.getVault().getPermissions();
            List<String> permissions = new ArrayList<>(((StringListData) mmoItem.getData(ItemStats.GRANTED_PERMISSIONS)).getList());
            permissions.forEach(s -> {
                this.data.permissions().remove(s);
                MMOItems.log("Perm removed: " + s);
                if (perms.has(this.data.getPlayer(), s))
                    perms.playerRemove(this.data.getPlayer(), s);
            });
        }

        // Abilities
        // TODO: find a solution for that:
        // Idea 1: cache ability uuid and remove it from the map
        if (mmoItem.hasData(ItemStats.ABILITIES))
            MODIFIERS_CACHE.getOrDefault(hashcode, Collections.emptyList())
                    .forEach(uuid -> this.data.getMMOPlayerData()
                            .getPassiveSkillMap()
                            .getModifiers()
                            .removeIf(passiveSkill -> passiveSkill.getUniqueId().equals(uuid)));
    }

    /**
     * Add all new item attributes & effects
     *
     * @param eItem The new item
     */
    private void addStats(@NotNull SlotEquippedItem eItem, int hashcode) {
        MMOItems.log("Adding new item attributes & effects");

        // Cache new item hashcode & item
        cache(eItem);

        // Check if the new item is empty
        if (isEmpty(eItem)) {
            MMOItems.log("New item is empty");
            return;
        }

        // Check if item is legal
        if (!eItem.isPlacementLegal() || !this.data.getRPG().canUse(eItem.getNBT(), false, false)) {
            if (!eItem.isPlacementLegal())
                MMOItems.log("Illegal item placement detected.");
            else
                MMOItems.log("Illegal item usage detected.");
            return;
        }

        // Cache item
        eItem.cacheItem();

        // Add item to MMO inventory
        this.data.getInventory().addItem(eItem);

        // Add all new item attributes & effects
        final MMOItem mmoItem = eItem.getCached();
        final EquipmentSlot equipmentSlot = eItem.getSlot();

        // Abilities
        MMOItems.log("Adding abilities");
        if (mmoItem.hasData(ItemStats.ABILITIES)) {
            for (AbilityData abilityData : ((AbilityListData) mmoItem.getData(ItemStats.ABILITIES)).getAbilities()) {

                MMOItems.log("Ability found: " + abilityData.getAbility().getName());
                ModifierSource modSource = eItem.getCached().getType().getModifierSource();
                PassiveSkill skill = this.data.getMMOPlayerData()
                        .getPassiveSkillMap()
                        .addModifier(new PassiveSkill("MMOItemsItem", abilityData, equipmentSlot, modSource));
                MODIFIERS_CACHE.computeIfAbsent(hashcode, i -> new ArrayList<>()).add(skill.getUniqueId());
                MMOItems.log("Ability added: " + abilityData.getTrigger());
            }
        } else
            MMOItems.log("No abilities found");

        // Modifier application rules
        final ModifierSource source = mmoItem.getType().getModifierSource();
        MMOItems.log("Modifier source: " + source.name());
        if (!EquipmentSlot.MAIN_HAND.isCompatible(source, equipmentSlot)) {
            MMOItems.log("Modifier source is not compatible with equipment slot");
            return;
        }

        // Apply permanent potion effects
        MMOItems.log("Adding permanent potion effects");
        if (mmoItem.hasData(ItemStats.PERM_EFFECTS)) {
            MMOItems.log("Permanent potion effects found");
            ((PotionEffectListData) mmoItem.getData(ItemStats.PERM_EFFECTS))
                    .getEffects()
                    .stream()
                    .filter(potionEffectData -> this.data.getPermanentPotionEffectAmplifier(potionEffectData.getType()) < potionEffectData.getLevel() - 1)
                    .peek(potionEffectData -> MMOItems.log("Pot effect added: " + potionEffectData.getType()))
                    .forEach(effect -> this.data.getPermanentPotionEffectsMap().put(effect.getType(), effect.toEffect()));
        } else
            MMOItems.log("No permanent potion effects found");

        // Permissions
        MMOItems.log("Adding permissions");
        if (MMOItems.plugin.hasPermissions() && mmoItem.hasData(ItemStats.GRANTED_PERMISSIONS)) {
            MMOItems.log("Permissions found");
            final Permission perms = MMOItems.plugin.getVault().getPermissions();
            this.data.permissions().addAll(((StringListData) mmoItem.getData(ItemStats.GRANTED_PERMISSIONS)).getList());
            this.data.permissions()
                    .stream()
                    .filter(s -> !perms.has(this.data.getPlayer(), s))
                    .peek(s -> MMOItems.log("Perm added: " + s))
                    .forEach(perm -> perms.playerAdd(this.data.getPlayer(), perm));
        } else
            MMOItems.log("No permissions found");
    }

    /**
     * Checks if the item needs to be updated
     *
     * @param item The item to check
     * @return True if the item needs to be updated
     */
    private boolean needsUpdate(@NotNull SlotEquippedItem item) {
        return !lastHashCodes.containsKey(item.getSlotNumber()) || lastHashCodes.get(item.getSlotNumber()) != (isEmpty(item) ? -1 : item.hashCode());
    }

    /**
     * Cache the item
     *
     * @param item The item to cache
     */
    private void cache(@NotNull SlotEquippedItem item) {
        final int hashCode = isEmpty(item) ? -1 : item.hashCode();
        lastHashCodes.put(item.getSlotNumber(), hashCode);
        CACHE.put(hashCode, item);
    }

    /**
     * Checks if the item is empty
     *
     * @param item The item to check
     * @return True if the item is empty
     */
    private boolean isEmpty(@Nullable SlotEquippedItem item) {
        return item == null || item.getNBT() == null || item.getNBT().getItem() == null || item.getNBT().getItem().getType().isAir();
    }

    public void start() {
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }
}
