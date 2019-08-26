package net.Indyuce.mmoitems.ability;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.manager.DamageManager.DamageType;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.VersionSound;

public class Ice_Crystal extends Ability {
	public Ice_Crystal() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 6);
		addModifier("duration", 3);
		addModifier("amplifier", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 1);
		new BukkitRunnable() {
			Vector vec = getTargetDirection(stats.getPlayer(), target).multiply(.45);
			Location loc = stats.getPlayer().getEyeLocation().clone().add(0, -.3, 0);
			int ti = 0;

			public void run() {
				ti++;
				if (ti > 25)
					cancel();

				loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 2, 1);
				List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
				for (int j = 0; j < 3; j++) {
					loc.add(vec);
					if (loc.getBlock().getType().isSolid())
						cancel();

					/*
					 * has a different particle effect since SNOW_DIG is not the
					 * same as in legacy minecraft, the particle effect is now a
					 * cross that rotates
					 */
					for (double r = 0; r < .4; r += .1)
						for (double a = 0; a < Math.PI * 2; a += Math.PI / 2) {
							Vector vec = MMOUtils.rotateFunc(new Vector(r * Math.cos(a + (double) ti / 10), r * Math.sin(a + (double) ti / 10), 0), loc);
							loc.add(vec);
							MMOItems.plugin.getVersion().getVersionWrapper().spawnParticle(Particle.REDSTONE, loc, .7f, Color.WHITE);
							loc.add(vec.multiply(-1));
						}

					for (Entity entity : entities)
						if (MMOUtils.canDamage(stats.getPlayer(), loc, entity)) {
							loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
							loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 48, 0, 0, 0, .2);
							loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
							MMOItems.plugin.getDamage().damage(stats, (LivingEntity) entity, data.getModifier("damage"), DamageType.MAGIC);
							((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (data.getModifier("duration") * 20), (int) data.getModifier("amplifier")));
							cancel();
							return;
						}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
