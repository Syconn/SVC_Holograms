package mod.syconn.svc.network.packets.client;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.utils.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Supplier;

public record MessagePlayerPacket(Component msg) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MessagePlayerPacket> TYPE = new CustomPacketPayload.Type<>(Constants.withId("message_player_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePlayerPacket> STREAM_CODEC = StreamCodec.composite(ComponentSerialization.TRUSTED_STREAM_CODEC, MessagePlayerPacket::msg, MessagePlayerPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MessagePlayerPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> context.getPlayer().sendSystemMessage(packet.msg));
    }
}
