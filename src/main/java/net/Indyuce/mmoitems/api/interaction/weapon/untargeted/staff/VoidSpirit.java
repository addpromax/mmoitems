package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.AttackResult.DamageType;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;

public class VoidSpirit implements StaffAttackHandler {

	@Override
	public void handle(TemporaryStats stats, NBTItem nbt, double attackDamage, double range) {
		Vector vec = stats.getPlayer().getEyeLocation().getDirection();
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
		ShulkerBullet shulkerBullet = (ShulkerBullet) stats.getPlayer().getWorld().spawnEntity(stats.getPlayer().getLocation().add(0, 1, 0), EntityType.valueOf("SHULKER_BULLET"));
		shulkerBullet.setShooter(stats.getPlayer());
		new BukkitRunnable() {
			double ti = 0;

			public void run() {
				ti += .1;
				if (shulkerBullet.isDead() || ti >= range / 4) {
					shulkerBullet.remove();
					cancel();
				}
				shulkerBullet.setVelocity(vec);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		MMOItems.plugin.getEntities().registerCustomEntity(shulkerBullet, new AttackResult(attackDamage, DamageType.WEAPON, DamageType.PHYSICAL, DamageType.PROJECTILE), 0., stats, nbt);
	}
}
