package mod.syconn.svc.server.savedData;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.block.WorldPos;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static mod.syconn.svc.server.savedData.extra.CallData.CallManager;

public class HologramNetwork extends SavedData {

    private static final String tagID = "hologram_network";
    private final CallManager manager = new CallManager();
//    private final ServerLevel level = null;

    public HologramNetwork() {}

    public void createCall(List<CallData.Callee> members, boolean secure) {
        this.manager.createCall(members, secure);
        this.setDirty();
    }

    public void connect(UUID callId, CallData.Callee callee) {
        this.manager.connectToCall(callId, callee);
        this.setDirty();
    }

    public void leaveCall(UUID callId, CallData.Callee callee) {
        this.manager.leaveCall(callId, callee);
        this.setDirty();
    }

    public void registerReceiver(UUID blockID, WorldPos pos) {
        this.manager.registerReceiver(blockID, pos);
        this.setDirty();
    }

    public void unregisterReceiver(UUID blockID) {
        this.manager.unregisterReceiver(blockID);
        this.setDirty();
    }

    public void playerLeftServer(UUID playerID) {
        this.manager.playerLeftServer(playerID);
        this.setDirty();
    }

    public CallData.BlockReceiver getCallForBlock(UUID receiverID) {
        return this.manager.getCallForBlock(receiverID);
    }

    public List<CallData.Call> getCallsForPlayer(UUID playerID) {
        return this.manager.getCallsForPlayer(playerID);
    }

    public Map<UUID, BlockPos> getDebugData() {
        return this.manager.getDebugData();
    }

    @Override
    public void setDirty() {
        System.out.println(GameInstance.getServer() + " From HOLONET SetDirty");

//        if (render() && level instanceof ServerLevel sl) sl.getPlayers(LivingEntity::isAlive).forEach(serverPlayer -> Network.CHANNEL.sendToPlayer(serverPlayer, new MessageUpdateClientPipeCache(getDataMap())));
        super.setDirty();
    }

    public void serverTick(ServerLevel level) { // TODO may not be needed anymore

    }

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
        return server.getServer().overworld().getDataStorage().computeIfAbsent(HologramNetwork::load, HologramNetwork::create, tagID);
    }
}
