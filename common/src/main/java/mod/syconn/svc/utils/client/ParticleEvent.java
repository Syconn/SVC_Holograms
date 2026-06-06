package mod.syconn.svc.utils.client;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;

public record ParticleEvent(Vec3 pos, Vec3 velocity, ParticleOptions type) {
}
