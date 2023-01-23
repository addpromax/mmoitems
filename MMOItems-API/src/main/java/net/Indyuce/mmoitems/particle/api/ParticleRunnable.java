package net.Indyuce.mmoitems.particle.api;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class ParticleRunnable extends BukkitRunnable {

    protected final ParticleData particle;
    protected final PlayerData player;

    public ParticleRunnable(ParticleData particle, PlayerData player) {
        this.particle = particle;
        this.player = player;
    }

    @Override
    public void run() {
        if (!player.isOnline()) return;
        createParticles();
    }

    public abstract void createParticles();

    public ParticleData getParticleData() {
        return particle;
    }
}
