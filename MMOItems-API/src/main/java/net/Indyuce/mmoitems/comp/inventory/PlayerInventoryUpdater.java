package net.Indyuce.mmoitems.comp.inventory;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.inventory.MMOItemEquipEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.comp.inventory.model.SlotEquippedItem;
import net.Indyuce.mmoitems.stat.data.*;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        MMOItems.plugin.getInventory()
                .inventory(data.getPlayer())
                .stream()
                .filter(item -> item instanceof SlotEquippedItem)
                .map(item -> (SlotEquippedItem) item)
                .filter(this::needsUpdate)
                .forEach(eItem -> {
                    final int currentHashcode = lastHashCodes.getOrDefault(eItem.getSlotNumber(), -99);
                    final int newHashcode = isEmpty(eItem) ? -1 : eItem.hashCode();
                    final SlotEquippedItem oldItem = CACHE.get(currentHashcode);

                    // Call item equip event
                    Bukkit.getPluginManager().callEvent(new MMOItemEquipEvent(currentHashcode, newHashcode, oldItem, eItem));
                    MMOItems.log("Calling item equip event");

                    // Remove all old item attributes & effects
                    if (oldItem != null && !isEmpty(oldItem)) {
                        MMOItems.log("Removing old item attributes & effects");
                        final MMOItem mmoItem = oldItem.getCached().clone();

                        // Potion effects
                        if (mmoItem.hasData(ItemStats.PERM_EFFECTS))
                            ((PotionEffectListData) mmoItem.getData(ItemStats.PERM_EFFECTS))
                                    .getEffects()
                                    .forEach(effect -> {
                                        this.data.getPermanentPotionEffectsMap().remove(effect.getType(), effect.toEffect());
                                        MMOItems.log("Pot effect removed: " + effect.getType());
                                    });

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
//                        if (mmoItem.hasData(ItemStats.ABILITIES)) {
//                            ModifierSource modSource = oldItem.getCached().getType().getModifierSource();
//                            ((AbilityListData) mmoItem.getData(ItemStats.ABILITIES))
//                                    .getAbilities()
//                                    .forEach(abilityData -> this.data.getMMOPlayerData()
//                                            .getPassiveSkillMap()
//                                            .getModifiers()
//                                            .removeIf(passiveSkill -> passiveSkill.getSource().equals(modSource)
//                                                    && passiveSkill.getType().equals(abilityData.getTrigger())));
//                        }
                    }

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

                    // Cache new item hashcode & item
                    cache(eItem);

                    // Add item to MMO inventory
                    this.data.getInventory().addItem(eItem);

                    // Add all new item attributes & effects
                    final MMOItem mmoItem = eItem.getCached().clone();
                    final EquipmentSlot equipmentSlot = eItem.getSlot();

                    // Abilities
                    if (mmoItem.hasData(ItemStats.ABILITIES)) {
                        for (AbilityData abilityData : ((AbilityListData) mmoItem.getData(ItemStats.ABILITIES)).getAbilities()) {
                            ModifierSource modSource = eItem.getCached().getType().getModifierSource();
                            this.data.getMMOPlayerData().getPassiveSkillMap().addModifier(new PassiveSkill("MMOItemsItem", abilityData, equipmentSlot, modSource));
                            MMOItems.log("Ability added: " + abilityData.getTrigger());
                        }
                    }

                    // Modifier application rules
                    final ModifierSource source = mmoItem.getType().getModifierSource();
                    if (!EquipmentSlot.MAIN_HAND.isCompatible(source, equipmentSlot)) {
                        MMOItems.log("Modifier source is not compatible with equipment slot");
                        return;
                    }

                    // Apply permanent potion effects
                    if (mmoItem.hasData(ItemStats.PERM_EFFECTS))
                        ((PotionEffectListData) mmoItem.getData(ItemStats.PERM_EFFECTS))
                                .getEffects()
                                .stream()
                                .filter(potionEffectData -> this.data.getPermanentPotionEffectAmplifier(potionEffectData.getType()) < potionEffectData.getLevel() - 1)
                                .peek(potionEffectData -> MMOItems.log("Pot effect added: " + potionEffectData.getType()))
                                .forEach(effect -> this.data.getPermanentPotionEffectsMap().put(effect.getType(), effect.toEffect()));

                    if (MMOItems.plugin.hasPermissions() && mmoItem.hasData(ItemStats.GRANTED_PERMISSIONS)) {
                        final Permission perms = MMOItems.plugin.getVault().getPermissions();
                        this.data.permissions().addAll(((StringListData) mmoItem.getData(ItemStats.GRANTED_PERMISSIONS)).getList());
                        this.data.permissions()
                                .stream()
                                .filter(s -> !perms.has(this.data.getPlayer(), s))
                                .peek(s -> MMOItems.log("Perm added: " + s))
                                .forEach(perm -> perms.playerAdd(this.data.getPlayer(), perm));
                    }
                });

        // TODO: Call inventory refresh event
        // Bukkit.getPluginManager().callEvent(new MMOInventoryRefreshEvent(inventory.equipped(), getPlayer(), this));
    }

    private boolean needsUpdate(@NotNull SlotEquippedItem item) {
        return !lastHashCodes.containsKey(item.getSlotNumber()) || lastHashCodes.get(item.getSlotNumber()) != (isEmpty(item) ? -1 : item.hashCode());
    }

    private void cache(@NotNull SlotEquippedItem item) {
        final int hashCode = isEmpty(item) ? -1 : item.hashCode();
        lastHashCodes.put(item.getSlotNumber(), hashCode);
        CACHE.put(hashCode, item);
        item.cacheItem();
    }

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
