package net.Indyuce.mmoitems.api.droptable.item;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.type.MMOItemType;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.RandomAmount;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MMOItemDropItem extends DropItem {
	private final MMOItemType type;
	private final String id;
	private final double unidentification;

	public MMOItemDropItem(MMOItemType type, String id, double drop, double unidentification, RandomAmount amount) {
		this(type, id, drop, unidentification, amount.getMin(), amount.getMax());
	}

	public MMOItemDropItem(MMOItemType type, String id, double drop, double unidentification, int min, int max) {
		super(drop, min, max);

		this.type = type;
		this.id = id;
		this.unidentification = unidentification;
	}

	public MMOItemDropItem(MMOItemType type, String id, String info) {
		super(info);

		this.type = type;
		this.id = id;

		String[] argSplit = info.split(",");
		unidentification = Double.parseDouble(argSplit[2]) / 100;
	}

	public MMOItemType getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public boolean rollIdentification() {
		return random.nextDouble() < unidentification;
	}

	@Override
	public ItemStack getItem(PlayerData player, int amount) {
		if (!MMOItems.plugin.getTemplates().hasTemplate(type, id))
			return null;

		ItemStack item = player == null ? MMOItems.plugin.getItem(type, id) : MMOItems.plugin.getItem(type, id, player);
		if (item == null || item.getType() == Material.AIR)
			return null;

		/*
		 * Apply unidentification before editing the item amount
		 */
		if (rollIdentification())
			item = type.getUnidentifiedTemplate().newBuilder(NBTItem.get(item)).build();

		item.setAmount(amount);
		return item;
	}

	@Override
	public String getKey() {
		return type.getId() + "." + id;
	}
}
