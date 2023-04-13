package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.armorequip.ArmorEquipEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.SoulboundInfo;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.util.InteractItem;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.DeathDowngrading;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerListener implements Listener {
    private final Map<Player, List<ItemStack>> deathItems = new HashMap<>();

    /**
     * Careful, MMOCore and MMOMana runs on LOWEST
     */
    @EventHandler(priority = EventPriority.LOW)
    public void loadPlayerData(PlayerJoinEvent event) {
        MMOItems.plugin.getRecipes().refreshRecipeBook(event.getPlayer());
        PlayerData.load(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void savePlayerData(PlayerQuitEvent event) {
        PlayerData.get(event.getPlayer()).save(true);
    }

    /**
     * If the player dies, its time to roll the death-downgrade stat!
     */
    @SuppressWarnings("InstanceofIncompatibleInterface")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeathForUpgradeLoss(@NotNull PlayerDeathEvent event) {

        // Supports NPCs
        if (!PlayerData.has(event.getEntity())) return;

        // See description of DelayedDeathDowngrade child class for full explanation
        (new DelayedDeathDowngrade(event)).runTaskLater(MMOItems.plugin, 3L);
    }

    /**
     * Prevent players from dropping items which are bound to them with a
     * soulbound. Items are cached inside a map waiting for the player to
     * respawn. If he does not respawn the items are dropped on the ground, this
     * way there don't get lost
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeathForSoulbound(PlayerDeathEvent event) {
        if (event.getKeepInventory() )
            return;

        Player player = event.getEntity();
        SoulboundInfo soulboundInfo = new SoulboundInfo(player);

        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            NBTItem nbt = NBTItem.get(item);

            if (nbt.hasTag("MMOITEMS_DISABLE_DEATH_DROP") && nbt.getBoolean("MMOITEMS_DISABLE_DEATH_DROP")) {
                iterator.remove();
                if (!deathItems.containsKey(player))
                    deathItems.put(player, new ArrayList<>());

                deathItems.get(player).add(item);
            }

            /*
             * not a perfect check but it's very sufficient and so we avoid
             * using a JsonParser followed by map checkups in the SoulboundData
             * constructor
             */
            else if (MMOItems.plugin.getLanguage().keepSoulboundOnDeath && nbt.hasTag("MMOITEMS_SOULBOUND") && nbt.getString("MMOITEMS_SOULBOUND").contains(player.getUniqueId().toString())) {
                iterator.remove();
                soulboundInfo.add(item);
            }
        }

        if (soulboundInfo.hasItems())
            soulboundInfo.setup();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (MMOItems.plugin.getLanguage().keepSoulboundOnDeath)
            SoulboundInfo.read(player);

        if (deathItems.containsKey(player)) {
            Bukkit.getScheduler().runTaskLater(MMOItems.plugin, () -> {
                player.getInventory().addItem(deathItems.get(player).toArray(new ItemStack[0]));
                deathItems.remove(player);
            }, 10);
        }
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent event) {
        Player player = event.getPlayer();
        NBTItem item = NBTItem.get(event.getNewArmorPiece());
        if (!PlayerData.get(player).getRPG().canUse(item, true))
            event.setCancelled(true);
    }

    /**
     * This handler listens to ALL trident shootings, including both
     * custom tridents from MMOItems AND vanilla tridents, since MMOItems
     * needs to apply on-hit effects like crits, elemental damage... even
     * if the player is using a vanilla trident.
     * <p>
     * Fixing commit 6cf6f741
     */
    @EventHandler(ignoreCancelled = true)
    public void registerTridents(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Trident) || !(event.getEntity().getShooter() instanceof Player))
            return;

        final InteractItem item = new InteractItem((Player) event.getEntity().getShooter(), Material.TRIDENT);
        if (!item.hasItem())
            return;

        final NBTItem nbtItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item.getItem());
        final Type type = Type.get(nbtItem.getType());
        final PlayerData playerData = PlayerData.get((Player) event.getEntity().getShooter());

        if (type != null) {
            final Weapon weapon = new Weapon(playerData, nbtItem);
            if (!weapon.checkItemRequirements() || !weapon.checkAndApplyWeaponCosts()) {
                event.setCancelled(true);
                return;
            }

            MMOItems.plugin.getEntities().registerCustomProjectile(nbtItem, playerData.getStats().newTemporary(EquipmentSlot.fromBukkit(item.getSlot())), event.getEntity(), 1);
        }
    }

    /**
     * Fixes an issue where quickly swapping items in hand just
     * does not update the player's inventory which can make the
     * player cast abilities or attacks with not the correct stats
     *
     * @deprecated This does cost some performance and that update
     * method NEEDS some improvement in the future
     */
    @Deprecated
    @EventHandler
    public void registerInventoryUpdates1(PlayerSwapHandItemsEvent event) {
        PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
    }

    /**
     * Fixes an issue where quickly swapping items in hand just
     * does not update the player's inventory which can make the
     * player cast abilities or attacks with not the correct stats
     *
     * @deprecated This does cost some performance and that update
     * method NEEDS some improvement in the future
     */
    @Deprecated
    @EventHandler
    public void registerInventoryUpdates2(PlayerItemHeldEvent event) {
        PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
    }

    /**
     * Some plugins like to interfere with dropping items when the
     * player dies, or whatever of that sort.
     * <p>
     * MMOItems would hate to dupe items because of this, as such, we wait
     * 3 ticks for those plugins to reasonably complete their operations and
     * then downgrade the items the player still has equipped.
     * <p>
     * If a plugin removes items in this time, they will be completely excluded
     * and no dupes will be caused, and if a plugin adds items, they will be
     * included and downgraded. I think that's reasonable behaviour.
     *
     * @author Gunging
     */
    private static class DelayedDeathDowngrade extends BukkitRunnable {

        @NotNull
        final PlayerDeathEvent event;

        DelayedDeathDowngrade(@NotNull PlayerDeathEvent event) {
            this.event = event;
        }

        @Override
        public void run() {

            // Downgrade player's inventory
            DeathDowngrading.playerDeathDowngrade(event.getEntity());
        }
    }
}
