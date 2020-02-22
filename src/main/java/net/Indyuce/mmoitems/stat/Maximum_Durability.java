package net.Indyuce.mmoitems.stat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.Conditional;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class Maximum_Durability extends DoubleStat implements Conditional {
	public Maximum_Durability() {
		super(new ItemStack(Material.SHEARS), "Maximum Durability", new String[] { "The amount of uses before your", "item becomes unusable/breaks." }, "max-durability", new String[] { "all" });
	}

	/*
	 * initializes the custom durability mecanism on an item.
	 */
	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		double value = ((DoubleData) data).generateNewValue();
		item.addItemTag(new ItemTag("MMOITEMS_MAX_DURABILITY", value), new ItemTag("MMOITEMS_DURABILITY", value));
		return true;
	}

	@Override
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
		if (item.hasTag("MMOITEMS_DURABILITY") && item.getDouble("MMOITEMS_DURABILITY") <= 0) {
			if (message) {
				Message.ZERO_DURABILITY.format(ChatColor.RED).send(player.getPlayer(), "cant-use-item");
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
			}
			return false;
		}
		return true;
	}
}