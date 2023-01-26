package net.Indyuce.mmoitems.particle.api;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class ParticleRunnable extends BukkitRunnable {

    private final UUID uniqueId;
    protected final ParticleData particle;
    protected final PlayerData player;

    public ParticleRunnable(ParticleData particle, PlayerData player) {
        this.particle = particle;
        this.player = player;
        this.uniqueId = UUID.randomUUID();
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

    public @NotNull UUID getUniqueId() {
        return uniqueId;
    }
}
