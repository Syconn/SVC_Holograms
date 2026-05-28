package mod.syconn.svc.network.packets.server;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class HoloCallPacket {

    private final Type type;
    private final UUID id;
    private final List<HologramNetwork.Caller> callers;

    public HoloCallPacket(Type type, UUID id, List<HologramNetwork.Caller> callers) {
        this.type = type;
        this.id = id;
        this.callers = callers;
    }

    public HoloCallPacket(FriendlyByteBuf buf) {
        this.type = buf.readEnum(Type.class);
        this.id = buf.readUUID();
        this.callers = NBTUtil.getList(buf.readNbt(), HologramNetwork.Caller::from);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.type);
        buf.writeUUID(this.id);
        buf.writeNbt(NBTUtil.putList(this.callers, HologramNetwork.Caller::save));
    }

    public void apply(Supplier<NetworkManager.PacketContext> context) {
        context.get().queue(() -> {
            if (context.get().getPlayer() instanceof ServerPlayer sp) {
                var network = HologramNetwork.get(sp.server.overworld());
                var caller = this.callers.get(0);
                if (this.type == Type.CREATE) network.createCall(this.callers.remove(0), this.callers);
                else if (this.type == Type.CONNECT) network.connect(this.id, caller);
                else network.leaveCall(this.id, caller);
            }
        });
    }

    public enum Type {
        CREATE,
        CONNECT,
        LEAVE
    }
}
