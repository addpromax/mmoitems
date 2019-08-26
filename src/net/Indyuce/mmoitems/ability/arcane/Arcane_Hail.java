package net.Indyuce.mmoitems.ability.arcane;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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

public class Arcane_Hail extends Ability {
	public Arcane_Hail() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 3);
		addModifier("duration", 4);
		addModifier("radius", 3);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		Location loc = getTargetLocation(stats.getPlayer(), target);
		if (loc == null) {
			result.setSuccessful(false);
			return;
		}

		double damage1 = data.getModifier("damage");
		double duration = data.getModifier("duration");
		double radius = data.getModifier("radius");

		new BukkitRunnable() {
			int j = 0;

			public void run() {
				j++;
				if (j > 10 * duration) {
					cancel();
					return;
				}

				Location loc1 = loc.clone().add(randomCoordMultiplier() * radius, 0, randomCoordMultiplier() * radius);
				loc1.getWorld().playSound(loc1, VersionSound.ENTITY_ENDERMAN_HURT.toSound(), 1, 0);
				for (Entity entity : MMOUtils.getNearbyChunkEntities(loc1))
					if (MMOUtils.canDamage(stats.getPlayer(), entity) && entity.getLocation().distanceSquared(loc1) <= 4)
						MMOItems.plugin.getDamage().damage(stats, (LivingEntity) entity, damage1, DamageType.MAGIC);
				loc1.getWorld().spawnParticle(Particle.SPELL_WITCH, loc1, 12, 0, 0, 0, .1);
				loc1.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc1, 6, 0, 0, 0, .1);

				Vector vector = new Vector(randomCoordMultiplier() * .03, .3, randomCoordMultiplier() * .03);
				for (double k = 0; k < 60; k++)
					loc1.getWorld().spawnParticle(Particle.SPELL_WITCH, loc1.add(vector), 0);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}

	// random double between -1 and 1
	private double randomCoordMultiplier() {
		return (random.nextDouble() - .5) * 2;
	}
}
