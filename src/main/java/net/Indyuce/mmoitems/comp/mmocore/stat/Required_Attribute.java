package net.Indyuce.mmoitems.comp.mmocore.stat;

import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.comp.mmocore.MMOCoreHook.MMOCoreRPGPlayer;
import net.Indyuce.mmoitems.stat.type.Conditional;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.version.VersionMaterial;

public class Required_Attribute extends DoubleStat implements Conditional {
	private final PlayerAttribute attribute;

	public Required_Attribute(PlayerAttribute attribute) {
		super(VersionMaterial.GRAY_DYE.toItem(), attribute.getName() + " Requirement (MMOCore)", new String[] { "Amount of " + attribute.getName() + " points the", "player needs to use the item." }, "required-" + attribute.getId());

		this.attribute = attribute;
	}

	@Override
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
		MMOCoreRPGPlayer mmocore = (MMOCoreRPGPlayer) player;
		return mmocore.getData().getAttributes().getAttribute(attribute) >= item.getStat(this);
	}
}
