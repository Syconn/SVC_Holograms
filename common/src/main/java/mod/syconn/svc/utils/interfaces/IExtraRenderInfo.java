package mod.syconn.svc.utils.interfaces;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface IExtraRenderInfo<T> {

    StreamCodec<RegistryFriendlyByteBuf, T> codec();

    void tickFakeEntity(@NotNull Entity entity);
    void updateFakeEntity(@NotNull Entity entity);
    void setupEntityOnCreate(@NotNull Entity entity);
    void getInfoServerSide(@NotNull Entity entity);

    class DefaultRenderInfo implements IExtraRenderInfo {
        @Override
        public StreamCodec codec() {
            return StreamCodec.unit();
        }

        @Override
        public void tickFakeEntity(@NotNull Entity entity) {}
        @Override
        public void updateFakeEntity(@NotNull Entity entity) {}
        @Override
        public void setupEntityOnCreate(@NotNull Entity entity) {}
        @Override
        public void getInfoServerSide(@NotNull Entity entity) {}
    }

}
