package mod.syconn.svc.utils.client;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public record ParticleEvent(Vec3 pos, Vec3 velocity, ParticleOptions type) {

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.put("pos", NBTUtil.putVec3(pos));
        tag.put("velocity", NBTUtil.putVec3(velocity));
        tag.putString("type", type.writeToString());
        return tag;
    }

    public static ParticleEvent from(CompoundTag tag) {
        try {
            return new ParticleEvent(NBTUtil.getVec3(tag.getCompound("pos")), NBTUtil.getVec3(tag.getCompound("velocity")), ParticleArgument.readParticle(new StringReader(tag.getString("type")), BuiltInRegistries.PARTICLE_TYPE.asLookup()));
        } catch (CommandSyntaxException e) {
            return null;
        }
    }
}
