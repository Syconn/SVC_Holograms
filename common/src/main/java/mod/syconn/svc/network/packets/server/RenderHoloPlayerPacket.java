package mod.syconn.svc.network.packets.server;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.utils.item.HologramTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

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
            else HologramTag.update(context.get().getPlayer().getUseItem(), tag -> tag.setSoloRender(this.name));
            System.out.println(context.get().getPlayer().getUseItem());
        });
    }
}
