package mod.syconn.svc.network.packets.server;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class RenderHoloPlayerPacket {

    private final BlockPos pos;
    private final String name;

    public RenderHoloPlayerPacket(BlockPos pos, String name) {
        this.pos = pos;
        this.name = name;
    }

    public RenderHoloPlayerPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.name = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.name);
    }

    public void apply(Supplier<NetworkManager.PacketContext> context) {
        context.get().queue(() -> {
            context.get().getPlayer().level().getBlockEntity(this.pos, ModBlockEntities.HOLO_PROJECTOR.get()).ifPresent(be -> be.setSoloRender(this.name, context.get().getPlayer().position()));
        });
    }
}
