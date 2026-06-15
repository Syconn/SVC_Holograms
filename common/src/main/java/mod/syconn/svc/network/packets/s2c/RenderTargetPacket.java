package mod.syconn.svc.network.packets.s2c;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.client.ClientRenderSystem;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.entity.RenderTargetInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RenderTargetPacket(RenderTargetInfo info) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RenderTargetPacket> TYPE = new CustomPacketPayload.Type<>(Constants.withId("render_target_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RenderTargetPacket> STREAM_CODEC = StreamCodec.composite(RenderTargetInfo.STREAM_CODEC, RenderTargetPacket::info, RenderTargetPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RenderTargetPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> ClientRenderSystem.get().handleRenderPlayerPacket(packet.info));
    }
}
