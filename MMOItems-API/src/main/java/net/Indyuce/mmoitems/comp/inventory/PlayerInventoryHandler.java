package net.Indyuce.mmoitems.comp.inventory;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.comp.inventory.model.PlayerInventoryImage;
import net.Indyuce.mmoitems.comp.inventory.model.PlayerMMOInventory;
import net.Indyuce.mmoitems.stat.data.*;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

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
        this.image = PlayerInventoryImage.make(data);
    }

    @Override
    public void run() {
        if (!data.getPlayer().isOnline()) this.running = false;
        if (!running) return;

        // Create a new image and compare it to the old one
        final PlayerInventoryImage newImage = PlayerInventoryImage.make(data);
        if (!newImage.isDifferent(image))
            return;

        newImage.difference(this.image)
                .forEach(item -> {
                    final int slot = item.getSlotNumber();
                    final VolatileMMOItem oldItem = image.getCached(slot).filter(volatileMMOItem -> !volatileMMOItem.getId().isEmpty()).orElse(null);
                    final VolatileMMOItem newItem = (item.getItem() == null || item.getItem().getType().isAir()) ? null : new VolatileMMOItem(item.getNBT());

                    // TODO: remove the following line
                    MMOItems.log(String.format("Slot #%d: %s -> %s", slot, oldItem == null ? "AIR" : oldItem.getId(), newItem == null ? "AIR" : newItem.getId()));

                    // Process old item
                    if (oldItem != null) {
                        // Potion effects
                        if (oldItem.hasData(ItemStats.PERM_EFFECTS))
                            ((PotionEffectListData) oldItem.getData(ItemStats.PERM_EFFECTS)).getEffects()
                                    .stream()
                                    .filter(e -> this.data.getPermanentPotionEffectAmplifier(e.getType()) == e.getLevel() - 1)
                                    .forEach(e -> this.data.getPermanentPotionEffectsMap().remove(e.getType(), e.toEffect()));

                        // Abilities

                        // Item particles
                        if (oldItem.hasData(ItemStats.ITEM_PARTICLES)) {
                            ParticleData particleData = (ParticleData) oldItem.getData(ItemStats.ITEM_PARTICLES);
                            if (particleData.getType().hasPriority())
                                this.data.resetOverridingItemParticles();
                            else
                                this.data.getItemParticles().removeIf(particleRunnable -> particleRunnable.getParticleData().equals(particleData));
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

                        // Remove the item from the inventory
                        this.inventory.remove(slot);
                    }

                    // Process new item
                    if (newItem != null) {
                        if (!item.isPlacementLegal() || !this.data.getRPG().canUse(item.getNBT(), false, false))
                            return;

                        // Cache item and add it to the inventory
                        item.cacheItem();
                        this.inventory.addItem(item);

                        // Abilities
                        if (newItem.hasData(ItemStats.ABILITIES))
                            for (AbilityData abilityData : ((AbilityListData) newItem.getData(ItemStats.ABILITIES)).getAbilities()) {
                                ModifierSource modSource = item.getCached().getType().getModifierSource();
                                this.data.getMMOPlayerData().getPassiveSkillMap().addModifier(new PassiveSkill("MMOItemsItem", abilityData, item.getSlot(), modSource));
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
                                if (this.data.getOverridingItemParticles() == null)
                                    this.data.setOverridingItemParticles(particleData.start(this.data));
                            } else
                                this.data.getItemParticles().add(particleData.start(this.data));
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
                });

        // Calculate player stats
        this.data.getStats().updateStats();

        // Update stats from external plugins
        MMOItems.plugin.getRPG().refreshStats(this.data);

        // Cache the new image
        this.image = newImage;
        this.image.cache();

        // Stop the task
        this.running = false;
    }

    /* Task */
    public boolean isRunning() {
        return running;
    }

    public void start() {
        running = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerInventoryHandler.this.run();
            }
        }.runTaskAsynchronously(MMOItems.plugin);
    }

    public void stop() {
        running = false;
    }
}
