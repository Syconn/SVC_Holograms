package mod.syconn.svc.network.packets.server;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.item.HoloProjectorItem;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.item.HologramComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

public record RenderHoloPlayerPacket(BlockPos pos, boolean itemMode, String name) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RenderHoloPlayerPacket> TYPE = new CustomPacketPayload.Type<>(Constants.withId("render_holo_player_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RenderHoloPlayerPacket> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, RenderHoloPlayerPacket::pos, ByteBufCodecs.BOOL,
            RenderHoloPlayerPacket::itemMode, ByteBufCodecs.STRING_UTF8, RenderHoloPlayerPacket::name, RenderHoloPlayerPacket::new);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RenderHoloPlayerPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var player = context.getPlayer();
            if (!packet.itemMode) player.level().getBlockEntity(packet.pos(), ModBlockEntities.HOLO_PROJECTOR.get()).ifPresent(be -> be.setSoloRender(packet.name(), player.position()));
            else {
                var stack = player.getItemInHand(InteractionHand.OFF_HAND);
                if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof HoloProjectorItem) stack = player.getItemInHand(InteractionHand.MAIN_HAND);
                HologramComponent.update(stack, component -> new HologramComponent(component.receiverID(), component.renderTarget(), packet.name()));
            }
        });
    }
}
