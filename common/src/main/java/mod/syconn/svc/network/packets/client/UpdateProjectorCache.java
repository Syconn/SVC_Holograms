package mod.syconn.svc.network.packets.client;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.client.render.debug.HoloProjectorDebugRenderer;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class UpdateProjectorCache {

    public Map<UUID, BlockPos> data;

    public UpdateProjectorCache(Map<UUID, BlockPos> data) {
        this.data = data;
    }

    public UpdateProjectorCache(FriendlyByteBuf buf) {
        this(NBTUtil.getMap(Objects.requireNonNull(buf.readNbt()), NBTUtil::getUUID, NbtUtils::readBlockPos));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(NBTUtil.putMap(this.data, NBTUtil::putUUID, NbtUtils::writeBlockPos));
    }

    public void apply(Supplier<NetworkManager.PacketContext> context) {
        context.get().queue(() -> {
            HoloProjectorDebugRenderer.requestedRefresh = true;
            HoloProjectorDebugRenderer.PROJECTORS = data;
        });
    }
}
