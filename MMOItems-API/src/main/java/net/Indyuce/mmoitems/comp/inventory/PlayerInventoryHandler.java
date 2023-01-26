package net.Indyuce.mmoitems.comp.inventory;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemSet;
import net.Indyuce.mmoitems.api.event.inventory.MMOInventoryRefreshEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import net.Indyuce.mmoitems.comp.inventory.model.PlayerInventoryImage;
import net.Indyuce.mmoitems.comp.inventory.model.PlayerMMOInventory;
import net.Indyuce.mmoitems.comp.inventory.model.SlotEquippedItem;
import net.Indyuce.mmoitems.particle.api.ParticleRunnable;
import net.Indyuce.mmoitems.stat.data.*;
import net.Indyuce.mmoitems.stat.type.AttackWeaponStat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * mmoitems
 * 17/01/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class PlayerInventoryHandler implements Runnable {

    private volatile boolean running = false;
    private final PlayerData data;
    private final Player player;
    private final PlayerMMOInventory inventory;
    private PlayerInventoryImage image;

    public PlayerInventoryHandler(@NotNull PlayerData data, @NotNull PlayerMMOInventory inventory) {
        this.data = data;
        this.inventory = inventory;
        this.player = data.getPlayer();
        this.image = new PlayerInventoryImage(this.data);
    }

    @Override
    public void run() {
        if (!data.getPlayer().isOnline()) this.running = false;
        if (!running) return;

        // Create a new image and compare it to the old one
        final PlayerInventoryImage newImage = PlayerInventoryImage.make(data);
        if (!newImage.isDifferent(image))
            return;

        AtomicBoolean armorChanged = new AtomicBoolean(false);
        newImage.difference(this.image)
                .forEach(item -> {
                    final int slot = item.getSlotNumber();
                    final VolatileMMOItem oldItem = image.getCached(slot).filter(volatileMMOItem -> !volatileMMOItem.getId().isEmpty()).orElse(null);
                    final VolatileMMOItem newItem = (item.getItem() == null || item.getItem().getType().isAir()) ? null : new VolatileMMOItem(item.getNBT());

                    if (item.getSlot().equals(EquipmentSlot.ARMOR))
                        armorChanged.set(true);

                    // Process old item
                    this.processOldItem(slot, oldItem);

                    // Process new item
                    this.processNewItem(newImage, item, newItem);
                });

        // If the armor changed, process the item sets
        if (armorChanged.get()) {
            // Process old item sets
            this.processOldItemSets();

            // Call Bukkit event
            Bukkit.getPluginManager().callEvent(new MMOInventoryRefreshEvent(inventory.equipped(), this.data.getPlayer(), this.data));

            // Process new item sets
            this.processNewItemSets(newImage);
        }

        // Check if the player is encumbered
        if (this.data.isEncumbered())
            this.player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, true, false));

        // Update stats from external plugins
        MMOItems.plugin.getRPG().refreshStats(this.data);

        // Cache the new image
        this.image = newImage;
        this.image.cache();

        // Stop the task
        this.running = false;
    }

    private void processOldItemSets() {
        final Map<ItemSet, Integer> itemSetCount = new HashMap<>();
        this.image.itemSets().forEach((s, integer) -> {
            final @Nullable ItemSet itemSet = MMOItems.plugin.getSets().get(s);
            if (itemSet == null)
                return;
            itemSetCount.put(itemSet, integer);
        });

        // Determine the bonuses to apply
        ItemSet.SetBonuses bonuses = null;
        for (Map.Entry<ItemSet, Integer> equippedSetBonus : itemSetCount.entrySet()) {
            if (bonuses == null)
                bonuses = equippedSetBonus.getKey().getBonuses(equippedSetBonus.getValue());
            else
                bonuses.merge(equippedSetBonus.getKey().getBonuses(equippedSetBonus.getValue()));
        }

        // Remove bonuses if there are any
        if (bonuses == null)
            return;

        // Stats
        ItemSet.SetBonuses finalBonuses = bonuses;
        MMOItems.plugin.getStats()
                .getNumericStats()
                .stream()
                .filter(finalBonuses::hasStat)
                .forEach(stat -> {
                    final StatInstance.ModifierPacket packet = this.data.getStats().getInstance(stat).newPacket();
                    packet.remove("MMOItemSetBonus");
                    packet.runUpdate();
                });


        // Permissions
        if (MMOItems.plugin.hasPermissions()) {
            final Permission perms = MMOItems.plugin.getVault().getPermissions();
            bonuses.getPermissions()
                    .stream()
                    .filter(s -> perms.has(this.player, s))
                    .forEach(s -> perms.playerRemove(this.player, s));
        }

        // Abilities
        this.image.setAbilities().forEach(uuid -> this.data.getMMOPlayerData().getPassiveSkillMap().removeModifier(uuid));

        // Particles
        this.image.particles()
                .getOrDefault(-99, new ArrayList<>())
                .forEach(uuid -> this.data.getItemParticles()
                        .stream()
                        .filter(particleRunnable -> particleRunnable.getUniqueId().equals(uuid))
                        .findFirst()
                        .ifPresent(particleRunnable -> {
                            particleRunnable.cancel();
                            this.data.getItemParticles().remove(particleRunnable);
                        }));

        // Potion effects
        bonuses.getPotionEffects()
                .forEach(effect -> {
                    this.data.getPermanentPotionEffectsMap().remove(effect.getType(), effect);
                    this.player.removePotionEffect(effect.getType());
                });
    }

    private void processNewItemSets(@NotNull PlayerInventoryImage newImage) {
        // Count the number of items in each set
        final Map<ItemSet, Integer> itemSetCount = new HashMap<>();
        for (EquippedItem equipped : inventory.equipped()) {
            final String tag = equipped.getCached().getNBT().getString("MMOITEMS_ITEM_SET");
            final @Nullable ItemSet itemSet = MMOItems.plugin.getSets().get(tag);
            if (itemSet == null)
                continue;
            itemSetCount.put(itemSet, itemSetCount.getOrDefault(itemSet, 0) + 1);
        }

        // Add item sets to the image
        itemSetCount.forEach((itemSet, integer) -> newImage.itemSets().put(itemSet.getId(), integer));
        // Determine the bonuses to apply
        ItemSet.SetBonuses bonuses = null;
        for (Map.Entry<ItemSet, Integer> equippedSetBonus : itemSetCount.entrySet()) {
            if (bonuses == null)
                bonuses = equippedSetBonus.getKey().getBonuses(equippedSetBonus.getValue());
            else
                bonuses.merge(equippedSetBonus.getKey().getBonuses(equippedSetBonus.getValue()));
        }

        // Apply the bonuses
        if (bonuses == null)
            return;

        // Stats
        final ItemSet.SetBonuses finalBonuses = bonuses;
        MMOItems.plugin.getStats()
                .getNumericStats()
                .stream()
                .filter(finalBonuses::hasStat)
                .forEach(stat -> {
                    final StatInstance.ModifierPacket packet = this.data.getStats().getInstance(stat).newPacket();
                    packet.addModifier(new StatModifier("MMOItemSetBonus", stat.getId(), finalBonuses.getStat(stat), ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER));
                    packet.runUpdate();
                });

        // Permissions
        if (MMOItems.plugin.hasPermissions()) {
            final Permission perms = MMOItems.plugin.getVault().getPermissions();
            for (String perm : bonuses.getPermissions())
                if (!perms.has(this.player, perm))
                    perms.playerAdd(this.player, perm);
        }

        // Abilities
        for (AbilityData ability : bonuses.getAbilities()) {
            PassiveSkill skill = this.data.getMMOPlayerData().getPassiveSkillMap().addModifier(new PassiveSkill("MMOItemsItem", ability, EquipmentSlot.OTHER, ModifierSource.OTHER));
            if (skill != null)
                newImage.setAbilities().add(skill.getUniqueId());
        }

        // Particles
        List<UUID> particleRunnables = new ArrayList<>();
        for (ParticleData particle : bonuses.getParticles()) {
            final ParticleRunnable pRunnable = particle.start(this.data);
            this.data.getItemParticles().add(pRunnable);
            particleRunnables.add(pRunnable.getUniqueId());
        }
        // -99 is a special value that means the particle is not tied to an item
        newImage.particles().put(-99, particleRunnables);

        // Potion effects
        for (PotionEffect effect : bonuses.getPotionEffects()) {
            if (this.data.getPermanentPotionEffectAmplifier(effect.getType()) < effect.getAmplifier())
                this.data.getPermanentPotionEffectsMap().put(effect.getType(), effect);
        }

    }

    private void processOldItem(int slot, @Nullable VolatileMMOItem oldItem) {
        if (oldItem == null)
            return;

        // Potion effects
        if (oldItem.hasData(ItemStats.PERM_EFFECTS))
            ((PotionEffectListData) oldItem.getData(ItemStats.PERM_EFFECTS)).getEffects()
                    .stream()
                    .filter(e -> this.data.getPermanentPotionEffectAmplifier(e.getType()) == e.getLevel() - 1)
                    .peek(potionEffectData -> this.player.removePotionEffect(potionEffectData.getType()))

                    .forEach(e -> this.data.getPermanentPotionEffectsMap().remove(e.getType(), e.toEffect()));

        // Abilities
        if (oldItem.hasData(ItemStats.ABILITIES))
            image.itemAbilities()
                    .getOrDefault(slot, new ArrayList<>())
                    .forEach(uuid -> this.data.getMMOPlayerData().getPassiveSkillMap().removeModifier(uuid));

        // Item particles
        if (oldItem.hasData(ItemStats.ITEM_PARTICLES)) {
            ParticleData particleData = (ParticleData) oldItem.getData(ItemStats.ITEM_PARTICLES);
            if (particleData.getType().hasPriority())
                this.data.resetOverridingItemParticles();
            else {
                this.image.particles()
                        .getOrDefault(slot, new ArrayList<>())
                        .forEach(uuid -> this.data.getItemParticles()
                                .stream()
                                .filter(particleRunnable -> particleRunnable.getUniqueId().equals(uuid))
                                .findFirst()
                                .ifPresent(particleRunnable -> {
                                    particleRunnable.cancel();
                                    this.data.getItemParticles().remove(particleRunnable);
                                }));
            }
        }

        // Permissions
        if (MMOItems.plugin.hasPermissions() && oldItem.hasData(ItemStats.GRANTED_PERMISSIONS)) {
            final Permission perms = MMOItems.plugin.getVault().getPermissions();
            ((StringListData) oldItem.getData(ItemStats.GRANTED_PERMISSIONS)).getList()
                    .forEach(s -> {
                        this.data.permissions().remove(s);
                        perms.playerRemove(player, s);
                    });
        }

        // Stats
        MMOItems.plugin.getStats()
                .getNumericStats()
                .stream()
                .filter(doubleStat -> oldItem.getNBT().getStat(doubleStat.getId()) > 0)
                .forEach(stat -> {
                    final StatInstance.ModifierPacket packet = this.data.getStats().getInstance(stat).newPacket();
                    packet.remove("MMOItem-" + slot);
                    packet.runUpdate();
                });

        // Remove the item from the inventory
        this.inventory.remove(slot);
    }

    private void processNewItem(@NotNull PlayerInventoryImage newImage, @Nullable SlotEquippedItem item, @Nullable VolatileMMOItem newItem) {
        if (newItem == null || item == null)
            return;
        if (!item.isPlacementLegal() || !this.data.getRPG().canUse(item.getNBT(), false, false))
            return;

        // Cache item and add it to the inventory
        item.cacheItem();
        this.inventory.addItem(item);

        // Stats
        MMOItems.plugin.getStats()
                .getNumericStats()
                .stream()
                .filter(doubleStat -> item.getNBT().getStat(doubleStat.getId()) > 0)
                .forEach(stat -> {
                    final StatInstance.ModifierPacket packet = this.data.getStats().getInstance(stat).newPacket();
                    final ModifierSource source = item.getCached().getType().getModifierSource();

                    double value = item.getNBT().getStat(stat.getId());

                    // Apply hand weapon stat offset
                    if (source.isWeapon() && stat instanceof AttackWeaponStat)
                        value -= ((AttackWeaponStat) stat).getOffset(this.data);

                    packet.addModifier(new StatModifier("MMOItem-" + item.getSlotNumber(), stat.getId(), value, ModifierType.FLAT, item.getSlot(), source));
                    packet.runUpdate();
                });

        // Abilities
        if (newItem.hasData(ItemStats.ABILITIES)) {
            List<UUID> uuids = new ArrayList<>();
            for (AbilityData abilityData : ((AbilityListData) newItem.getData(ItemStats.ABILITIES)).getAbilities()) {
                ModifierSource modSource = item.getCached().getType().getModifierSource();
                PassiveSkill skill = this.data.getMMOPlayerData().getPassiveSkillMap().addModifier(new PassiveSkill("MMOItemsItem", abilityData, item.getSlot(), modSource));
                if (skill != null)
                    uuids.add(skill.getUniqueId());
            }
            newImage.itemAbilities().put(item.getSlotNumber(), uuids);
        }

        // Modifier application rules
        final ModifierSource source = newItem.getType().getModifierSource();
        final EquipmentSlot equipmentSlot = item.getSlot();
        if (!EquipmentSlot.MAIN_HAND.isCompatible(source, equipmentSlot))
            return;

        // Potion effects
        if (newItem.hasData(ItemStats.PERM_EFFECTS))
            ((PotionEffectListData) newItem.getData(ItemStats.PERM_EFFECTS)).getEffects()
                    .stream()
                    .filter(e -> this.data.getPermanentPotionEffectAmplifier(e.getType()) < e.getLevel() - 1)
                    .forEach(effect -> this.data.getPermanentPotionEffectsMap().put(effect.getType(), effect.toEffect()));

        // Item particles
        if (newItem.hasData(ItemStats.ITEM_PARTICLES)) {
            ParticleData particleData = (ParticleData) newItem.getData(ItemStats.ITEM_PARTICLES);
            if (particleData.getType().hasPriority()) {
                if (this.data.getOverridingItemParticles() == null) {
                    ParticleRunnable particleRunnable = particleData.start(this.data);
                    newImage.particles().computeIfAbsent(item.getSlotNumber(), k -> new ArrayList<>()).add(particleRunnable.getUniqueId());
                    this.data.setOverridingItemParticles(particleRunnable);
                }
            } else {
                ParticleRunnable particleRunnable = particleData.start(this.data);
                newImage.particles().computeIfAbsent(item.getSlotNumber(), k -> new ArrayList<>()).add(particleRunnable.getUniqueId());
                this.data.setOverridingItemParticles(particleRunnable);
            }
        }

        // Permissions
        if (MMOItems.plugin.hasPermissions() && newItem.hasData(ItemStats.GRANTED_PERMISSIONS)) {
            final Permission perms = MMOItems.plugin.getVault().getPermissions();
            this.data.permissions().addAll(((StringListData) newItem.getData(ItemStats.GRANTED_PERMISSIONS)).getList());
            this.data.permissions()
                    .stream()
                    .filter(s -> !perms.has(player, s))
                    .forEach(perm -> perms.playerAdd(player, perm));
        }
    }

    /* Task */
    public boolean isRunning() {
        return running;
    }

    public void start() {
        running = true;
        if (Bukkit.isPrimaryThread())
            run();
        else
            new BukkitRunnable() {
                @Override
                public void run() {
                    PlayerInventoryHandler.this.run();
                }
            }.runTask(MMOItems.plugin);
    }

    public void stop() {
        running = false;
    }
}
