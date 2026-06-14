package mod.syconn.svc.network.packets.server;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.blockentity.HoloProjectorBlockEntity;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.generic.StreamCodecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record HoloCallPacket(PacketCallType callType, UUID id, boolean secure, boolean unknownID, BlockPos pos, List<CallData.Callee> callees) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<HoloCallPacket> TYPE = new CustomPacketPayload.Type<>(Constants.withId("holo_call_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HoloCallPacket> STREAM_CODEC = StreamCodec.composite(StreamCodecUtil.enumCodec(PacketCallType.class), HoloCallPacket::callType, UUIDUtil.STREAM_CODEC, HoloCallPacket::id,
            ByteBufCodecs.BOOL, HoloCallPacket::secure, ByteBufCodecs.BOOL, HoloCallPacket::unknownID, BlockPos.STREAM_CODEC, HoloCallPacket::pos, ByteBufCodecs.collection(ArrayList::new, CallData.Callee.STREAM_CODEC),
            HoloCallPacket::callees, HoloCallPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HoloCallPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                var network = HologramNetwork.get(sp.server.overworld());
                var caller = packet.callees.getFirst();
                if (packet.callType == PacketCallType.CREATE) network.createCall(packet.callees, packet.secure);
                else if (packet.callType == PacketCallType.CONNECT) network.connect(packet.id, caller);
                else if (packet.unknownID && sp.level().getBlockEntity(packet.pos) instanceof HoloProjectorBlockEntity be) {
                    var uuid = be.getReceiverUUID();
                    if (network.getBlockReceiver(uuid) != null) network.leaveCall(network.getBlockReceiver(uuid).callID, caller);
                } else network.leaveCall(packet.id, caller);
            }
        });
    }
}

