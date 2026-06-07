package mod.syconn.svc.network.packets.client;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.client.screen.HologramScreen;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class RequestedHologramPacket {

    private final CompoundTag tag;

    public RequestedHologramPacket(CompoundTag tag) {
        this.tag = tag;
    }

    public RequestedHologramPacket(FriendlyByteBuf buf) {
        this.tag = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(this.tag);
    }

    public void apply(Supplier<NetworkManager.PacketContext> context) {
        context.get().queue(() -> { if (GameInstance.getClient().screen instanceof HologramScreen screen) screen.hologramData(NBTUtil.getList(tag, CallData.Call::from)); });
    }
}
