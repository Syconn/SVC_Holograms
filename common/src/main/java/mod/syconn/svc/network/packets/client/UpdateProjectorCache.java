package mod.syconn.svc.network.packets.client;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.client.render.debug.HoloProjectorDebugRenderer;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.network.FriendlyByteBuf;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class UpdateProjectorCache {

    public List<CallData.BlockReceiver> data;

    public UpdateProjectorCache(List<CallData.BlockReceiver> data) {
        this.data = data;
    }

    public UpdateProjectorCache(FriendlyByteBuf buf) {
        this(NBTUtil.getList(Objects.requireNonNull(buf.readNbt()), CallData.BlockReceiver::from));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(NBTUtil.putList(this.data, CallData.BlockReceiver::save));
    }

    public void apply(Supplier<NetworkManager.PacketContext> context) {
        context.get().queue(() -> HoloProjectorDebugRenderer.PROJECTORS = data);
    }
}
