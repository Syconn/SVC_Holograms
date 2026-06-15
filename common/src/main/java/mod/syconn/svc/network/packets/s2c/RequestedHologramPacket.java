package mod.syconn.svc.network.packets.s2c;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.client.screen.HologramScreen;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestedHologramPacket(CompoundTag tag) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestedHologramPacket> TYPE = new CustomPacketPayload.Type<>(Constants.withId("requested_hologram_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestedHologramPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, RequestedHologramPacket::tag, RequestedHologramPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return null;
    }

    public static void handle(RequestedHologramPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> { if (GameInstance.getClient().screen instanceof HologramScreen screen) screen.hologramData(NBTUtil.getList(packet.tag, CallData.Call::from)); });
    }
}
