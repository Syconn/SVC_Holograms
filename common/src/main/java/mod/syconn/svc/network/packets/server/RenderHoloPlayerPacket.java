package mod.syconn.svc.network.packets.server;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.item.HoloProjectorItem;
import mod.syconn.svc.utils.item.HologramTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class RenderHoloPlayerPacket {

    private final BlockPos pos;
    private final boolean itemMode;
    private final String name;

    public RenderHoloPlayerPacket(BlockPos pos, boolean itemMode, String name) {
        this.pos = pos;
        this.itemMode = itemMode;
        this.name = name;
    }

    public RenderHoloPlayerPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.itemMode = buf.readBoolean();
        this.name = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.itemMode);
        buf.writeUtf(this.name);
    }

    public void apply(Supplier<NetworkManager.PacketContext> context) {
        context.get().queue(() -> {
            if (!this.itemMode) context.get().getPlayer().level().getBlockEntity(this.pos, ModBlockEntities.HOLO_PROJECTOR.get()).ifPresent(be -> be.setSoloRender(this.name, context.get().getPlayer().position()));
            else {
                var stack = context.get().getPlayer().getItemInHand(InteractionHand.OFF_HAND);
                if (context.get().getPlayer().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof HoloProjectorItem) stack = context.get().getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
                HologramTag.update(stack, tag -> tag.setSoloRender(this.name));
            }
        });
    }
}
