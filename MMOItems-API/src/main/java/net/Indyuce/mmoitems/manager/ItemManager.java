package net.Indyuce.mmoitems.manager;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.type.MMOItemType;
import org.bukkit.inventory.ItemStack;

/**
 * ItemStack and MMOItem getters were moved over to MMOItems. There is no longer
 * any item manager because the normal items are now all item templates. All
 * register methods are now in the TemplateManager.
 * 
 * @author cympe
 */
public class ItemManager {

	/**
	 * @param      type The item type
	 * @param      id   The item id
	 * @return          The corresponding MMOItem
	 * @deprecated      Use MMOItems.plugin.getMMOItem(Type, String) instead
	 */
	@Deprecated
	public MMOItem getMMOItem(MMOItemType type, String id) {
		return MMOItems.plugin.getMMOItem(type, id);
	}

	/**
	 * @param      type The item type
	 * @param      id   The item id
	 * @return          Generates an ItemStack using an MMOItem
	 * @deprecated      Use MMOItems.plugin.getItem(Type, String) instead
	 */
	@Deprecated
	public ItemStack getItem(MMOItemType type, String id) {
		return MMOItems.plugin.getItem(type, id);
	}
}
