package mod.syconn.svc.utils.client;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.phys.Vec3;

public record ParticleEvent(Vec3 pos, Vec3 velocity, ParticleOptions type) {

    public CompoundTag save(HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        tag.put("pos", NBTUtil.putVec3(pos));
        tag.put("velocity", NBTUtil.putVec3(velocity));
        tag.put("type", ParticleTypes.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), type).result().orElseThrow());
        return tag;
    }

    public static ParticleEvent from(CompoundTag tag, HolderLookup.Provider registries) {
        return new ParticleEvent(NBTUtil.getVec3(tag.getCompound("pos")), NBTUtil.getVec3(tag.getCompound("velocity")),
                ParticleTypes.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("particle")).result().orElse(null));
    }
}
