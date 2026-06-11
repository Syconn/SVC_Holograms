package mod.syconn.svc.utils.interfaces;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface IExtraRenderInfo {

    void tickFakeEntity(@NotNull Entity entity);
    void updateFakeEntity(@NotNull Entity entity);
    void setupEntityOnCreate(@NotNull Entity entity);
    void getInfoServerSide(@NotNull Entity entity);
    void getInfoClientSide(FriendlyByteBuf buffer);
    void encodeInfoServerSide(FriendlyByteBuf buffer);

    class DefaultRenderInfo implements IExtraRenderInfo {
        @Override
        public void tickFakeEntity(@NotNull Entity entity) {}
        @Override
        public void updateFakeEntity(@NotNull Entity entity) {}
        @Override
        public void setupEntityOnCreate(@NotNull Entity entity) {}
        @Override
        public void getInfoServerSide(@NotNull Entity entity) {}
        @Override
        public void getInfoClientSide(FriendlyByteBuf buffer) {}
        @Override
        public void encodeInfoServerSide(FriendlyByteBuf buffer) {}
    }

}
