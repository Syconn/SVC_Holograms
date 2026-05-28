package mod.syconn.svc.server.savedData;

import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.block.WorldPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static mod.syconn.svc.server.savedData.extra.CallData.CallManager;

public class HologramNetwork extends SavedData {

    private static final String tagID = "hologram_network";

    private final CallManager manager = new CallManager();

    public HologramNetwork() { }

    public void createCall(List<CallData.Callee> members) {

    }

    public void connect(UUID callId, Caller caller) {

    }

    public void leaveCall(UUID callId, Caller caller) {

    }

    public void registerReceiver(UUID blockID, WorldPos pos) {
        manager.registerReceiver(blockID, pos);
    }

    public void unregisterReceiver(UUID blockID) {
        manager.unregisterReceiver(blockID);
    }

    public void serverTick(ServerLevel level) {
//        cleanCalls(level);
//        createCallData(level);
    }

//    private void cleanCalls(ServerLevel level) {
//        var onlinePlayers = level.players().stream().map(Entity::getUUID).toList();
//        var calls = Map.copyOf(this.CALLS);
//        calls.forEach(((uuid, call) -> {
//            if (!onlinePlayers.contains(call.owner.uuid)) this.leaveCall(uuid, call.owner);
//            else {
//                call.participants.forEach(((uuid1, caller) -> {
//                    if (!onlinePlayers.contains(caller.uuid)) this.leaveCall(uuid, caller);
//                }));
//            }
//        }));
//    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag) {
        compoundTag.put("manager", manager.save());
        return compoundTag;
    }

    private void read(CompoundTag tag) {
        manager.read(tag.getCompound("manager"));
    }

    public static HologramNetwork load(CompoundTag tag) {
        var network = create();
        network.read(tag);
        return network;
    }

    private static HologramNetwork create() {
        return new HologramNetwork();
    }

    public static HologramNetwork get(ServerLevel server) {
        return server.getDataStorage().computeIfAbsent(HologramNetwork::load, HologramNetwork::create, tagID);
    }
}
