package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.item.type.MMOItemType;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stats which implement a consumable action like deconstructing, identifying,
 * applying a skin onto an item...
 * 
 * @author cympe
 *
 */
public interface ConsumableItemInteraction {

	/**
	 * Applies a consumable effect onto the item.
	 * 
	 * @param  event      The click event
	 * @param  playerData The player applying the consumable
	 * @param  consumable The consumable being applied with a VolatileMMOItem
	 *                    stored inside
	 * @param  target     The target item
	 * @param  targetType The item type of target item
	 * @return            True if the consumable effect was successfully applied
	 *                    (basically return true if it should be the only
	 *                    consumable effect applied).
	 */
	boolean handleConsumableEffect(@NotNull InventoryClickEvent event, @NotNull PlayerData playerData, @NotNull Consumable consumable, @NotNull NBTItem target, @Nullable MMOItemType targetType);
}
