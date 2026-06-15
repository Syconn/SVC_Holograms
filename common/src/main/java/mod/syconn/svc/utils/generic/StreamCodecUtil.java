package mod.syconn.svc.utils.generic;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class StreamCodecUtil {

    public static final StreamCodec<ByteBuf, Vec3> VEC3 = StreamCodec.composite(ByteBufCodecs.DOUBLE, Vec3::x, ByteBufCodecs.DOUBLE, Vec3::y, ByteBufCodecs.DOUBLE, Vec3::z, Vec3::new);

    public static <T extends Enum<T>> StreamCodec<ByteBuf, T> enumCodec(Class<T> type) {
        return new StreamCodec<>() {

            @Override
            public T decode(ByteBuf byteBuf) {
                return type.getEnumConstants()[byteBuf.readInt()];
            }

            @Override
            public void encode(ByteBuf byteBuf, T type) {
                byteBuf.writeInt(type.ordinal());
            }
        };
    }
}
