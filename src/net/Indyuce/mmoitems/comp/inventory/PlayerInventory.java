package net.Indyuce.mmoitems.comp.inventory;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.Type.EquipmentSlot;
import net.Indyuce.mmoitems.api.item.NBTItem;

public interface PlayerInventory {
	public List<EquippedItem> getInventory(Player player);

	public class EquippedItem {
		private final ItemStack item;
		private final EquipmentSlot slot;

		public EquippedItem(ItemStack item, EquipmentSlot slot) {
			this.item = item;
			this.slot = slot;
		}

		public NBTItem newNBTItem() {
			return MMOItems.plugin.getNMS().getNBTItem(item);
		}

		public boolean matches(Type type) {
			return slot == EquipmentSlot.ANY || (type.getEquipmentType() == EquipmentSlot.BOTH_HANDS ? slot.isHand() : slot == EquipmentSlot.BOTH_HANDS ? type.getEquipmentType().isHand() : slot == type.getEquipmentType());
		}
	}
}
