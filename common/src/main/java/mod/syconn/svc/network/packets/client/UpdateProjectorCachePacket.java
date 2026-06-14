package mod.syconn.svc.network.packets.client;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.client.render.debug.HoloProjectorDebugRenderer;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.entity.RenderTargetInfo;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public record UpdateProjectorCachePacket(List<CallData.BlockReceiver> data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateProjectorCachePacket> TYPE = new CustomPacketPayload.Type<>(Constants.withId("update_projector_cache_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateProjectorCachePacket> STREAM_CODEC = StreamCodec.composite(CallData.BlockReceiver.STREAM_CODEC.apply(ByteBufCodecs.list()),
            UpdateProjectorCachePacket::data, UpdateProjectorCachePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return null;
    }

    public static void handle(UpdateProjectorCachePacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> HoloProjectorDebugRenderer.PROJECTORS = packet.data);
    }
}
