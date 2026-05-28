package mod.syconn.svc.network.packets.server;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class HoloCallPacket {

    private final Type type;
    private final UUID id;
    private final boolean secure;
    private final List<CallData.Callee> callees;

    public HoloCallPacket(Type type, UUID id, boolean secure, List<CallData.Callee> callees) {
        this.type = type;
        this.id = id;
        this.secure = secure;
        this.callees = callees;
    }

    public HoloCallPacket(FriendlyByteBuf buf) {
        this.type = buf.readEnum(Type.class);
        this.id = buf.readUUID();
        this.secure = buf.readBoolean();
        var nbt = buf.readNbt();
        this.callees = NBTUtil.getList(nbt != null ? nbt : new CompoundTag(), CallData.Callee::from);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.type);
        buf.writeUUID(this.id);
        buf.writeBoolean(this.secure);
        buf.writeNbt(NBTUtil.putList(this.callees, CallData.Callee::save));
    }

    public void apply(Supplier<NetworkManager.PacketContext> context) {
        context.get().queue(() -> {
            if (context.get().getPlayer() instanceof ServerPlayer sp) {
                var network = HologramNetwork.get(sp.server.overworld());
                var caller = this.callees.get(0);
                if (this.type == Type.CREATE) network.createCall(this.callees, this.secure);
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
