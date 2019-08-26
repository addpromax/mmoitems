package net.Indyuce.mmoitems.particle.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.StringValue;
import net.Indyuce.mmoitems.particle.AuraParticles;
import net.Indyuce.mmoitems.particle.DoubleRingsParticles;
import net.Indyuce.mmoitems.particle.FirefliesParticles;
import net.Indyuce.mmoitems.particle.GalaxyParticles;
import net.Indyuce.mmoitems.particle.HelixParticles;
import net.Indyuce.mmoitems.particle.OffsetParticles;
import net.Indyuce.mmoitems.particle.VortexParticles;
import net.Indyuce.mmoitems.stat.data.ParticleData;

public enum ParticleType {
	OFFSET((particle, player) -> new OffsetParticles(particle, player), false, 5, "Some particles randomly spawning around your body.", new StringValue("amount", 5), new StringValue("vertical-offset", .5), new StringValue("horizontal-offset", .3), new StringValue("speed", 0), new StringValue("height", 1)),
	FIREFLIES((particle, player) -> new FirefliesParticles(particle, player), true, 1, "Particles dashing around you at the same height.", new StringValue("amount", 3), new StringValue("speed", 0), new StringValue("rotation-speed", 1), new StringValue("radius", 1.3), new StringValue("height", 1)),
	VORTEX((particle, player) -> new VortexParticles(particle, player), true, 1, "Particles flying around you in a cone shape.", new StringValue("radius", 1.5), new StringValue("height", 2.4), new StringValue("speed", 0), new StringValue("y-speed", 1), new StringValue("rotation-speed", 1), new StringValue("amount", 3)),
	GALAXY((particle, player) -> new GalaxyParticles(particle, player), true, 1, "Particles flying around you in spiral arms.", new StringValue("height", 1), new StringValue("speed", 1), new StringValue("y-coord", 0), new StringValue("rotation-speed", 1), new StringValue("amount", 6)),
	DOUBLE_RINGS((particle, player) -> new DoubleRingsParticles(particle, player), true, 1, "Particles drawing two rings around you.", new StringValue("radius", .8), new StringValue("y-offset", .4), new StringValue("height", 1), new StringValue("speed", 0), new StringValue("rotation-speed", 1)),
	HELIX((particle, player) -> new HelixParticles(particle, player), true, 1, "Particles drawing a sphere around you.", new StringValue("radius", .8), new StringValue("height", .6), new StringValue("rotation-speed", 1), new StringValue("y-speed", 1), new StringValue("amount", 4), new StringValue("speed", 0)),
	AURA((particle, player) -> new AuraParticles(particle, player), true, 1, "Particles dashing around you (height can differ).", new StringValue("amount", 3), new StringValue("speed", 0), new StringValue("rotation-speed", 1), new StringValue("y-speed", 1), new StringValue("y-offset", .7), new StringValue("radius", 1.3), new StringValue("height", 1));

	/*
	 * if override is set to true, only one particle effect can play at a time,
	 * and the particle which overrides has priority
	 */
	private boolean override;

	/*
	 * list of double modifiers that allow to configurate the particle effects,
	 * they'll be displayed in the effect editor once the particle type is
	 * chosen.
	 */
	private Map<String, Double> modifiers = new HashMap<>();

	private long period;
	private BiFunction<ParticleData, PlayerData, ParticleRunnable> func;
	private String lore;

	private ParticleType(BiFunction<ParticleData, PlayerData, ParticleRunnable> func, boolean override, long period, String lore, StringValue... modifiers) {
		this.func = func;
		this.override = override;
		this.period = period;
		this.lore = lore;

		for (StringValue modifier : modifiers)
			this.modifiers.put(modifier.getName(), modifier.getValue());
	}

	public String getDefaultName() {
		return MMOUtils.caseOnWords(name().toLowerCase().replace("_", " "));
	}

	public double getModifier(String path) {
		return modifiers.get(path);
	}

	public Set<String> getModifiers() {
		return modifiers.keySet();
	}

	public String getDescription() {
		return lore;
	}

	public boolean hasPriority() {
		return override;
	}

	public long getTime() {
		return period;
	}

	public ParticleRunnable newRunnable(ParticleData particle, PlayerData player) {
		return func.apply(particle, player);
	}
}