package mod.syconn.svc.network.packets.c2s;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.network.packets.s2c.RequestedHologramPacket;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record RequestHologramPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestHologramPacket> TYPE = new CustomPacketPayload.Type<>(Constants.withId("request_hologram_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestHologramPacket> STREAM_CODEC = StreamCodec.unit(new RequestHologramPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestHologramPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> { if (context.getPlayer() instanceof ServerPlayer sp) NetworkManager.sendToPlayer(sp, new RequestedHologramPacket(NBTUtil.putList(HologramNetwork.get(sp.server.overworld()).getCallsForPlayer(sp.getUUID()), CallData.Call::save))); });
    }
}
