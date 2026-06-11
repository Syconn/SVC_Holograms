package mod.syconn.svc.network.packets.client;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.client.ClientRenderSystem;
import mod.syconn.svc.utils.entity.RenderTargetInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class RenderTargetPacket {

    private final RenderTargetInfo info;

    public RenderTargetPacket(@NotNull Entity target) {
        info = new RenderTargetInfo(target);
    }

    public RenderTargetPacket(FriendlyByteBuf buffer) {
        info = new RenderTargetInfo(buffer);
    }

    public void encode(FriendlyByteBuf buffer) {
        info.encode(buffer);
    }

    public void apply(Supplier<NetworkManager.PacketContext> context) {
        context.get().queue(() -> ClientRenderSystem.get().handleRenderPlayerPacket(info));
    }
}
