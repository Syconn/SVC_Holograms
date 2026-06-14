package mod.syconn.svc.utils.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record WorldPos(ResourceKey<Level> level, BlockPos pos) {

    public static final StreamCodec<RegistryFriendlyByteBuf, WorldPos> STREAM_CODEC = StreamCodec.composite(ResourceKey.streamCodec(Registries.DIMENSION), WorldPos::level, BlockPos.STREAM_CODEC, WorldPos::pos, WorldPos::new);

    public static WorldPos from(CompoundTag tag) {
        return new WorldPos(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("level"))), NbtUtils.readBlockPos(tag, "pos").orElse(null));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorldPos(ResourceKey<Level> level1, BlockPos pos1)) {
            return level1.equals(this.level) && pos1.equals(this.pos);
        }
        return false;
    }

    public Vec3 toVector() {
        return new Vec3(this.pos.getX(), this.pos.getY(), this.pos.getZ());
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putString("level", this.level.location().toString());
        tag.put("pos", NbtUtils.writeBlockPos(this.pos));
        return tag;
    }
}
