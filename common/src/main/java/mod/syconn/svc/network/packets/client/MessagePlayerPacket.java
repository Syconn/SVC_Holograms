package mod.syconn.svc.network.packets.client;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class MessagePlayerPacket {

    private final Component msg;

    public MessagePlayerPacket(Component msg) {
        this.msg = msg;
    }

    public MessagePlayerPacket(FriendlyByteBuf buf) {
        this.msg = buf.readComponent();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeComponent(this.msg);
    }

    public void apply(Supplier<NetworkManager.PacketContext> context) {
        context.get().queue(() -> context.get().getPlayer().sendSystemMessage(this.msg));
    }
}
