package mod.syconn.svc.server.savedData;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.network.Network;
import mod.syconn.svc.network.packets.client.UpdateProjectorCache;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.block.WorldPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static mod.syconn.svc.server.savedData.extra.CallData.CallManager;

public class HologramNetwork extends SavedData {

    private static final String tagID = "hologram_network";
    private final CallManager manager = new CallManager();

    public HologramNetwork() { }

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

    public void addNewRenderMember(UUID callID, UUID playerID, Vec3 pos) {
        this.manager.addNewRenderMember(callID, playerID, pos);
        this.setDirty();
    }

    public CallData.Call getCall(UUID callID) {
        return this.manager.getCall(callID);
    }

    public CallData.BlockReceiver getBlockReceiver(UUID receiverID) {
        return this.manager.getBlockReceiver(receiverID);
    }

    public List<CallData.Call> getCallsForPlayer(UUID playerID) {
        return this.manager.getCallsForPlayer(playerID);
    }

    public List<CallData.BlockReceiver> getDebugData() {
        return this.manager.getDebugData();
    }

    @Override
    public void setDirty() {
        this.manager.validateCallLog();
        this.manager.validateReceivers();
        var server = GameInstance.getServer();
        if (server != null)
            server.overworld().getPlayers(LivingEntity::isAlive).forEach(serverPlayer -> Network.CHANNEL.sendToPlayer(serverPlayer, new UpdateProjectorCache(this.getDebugData())));
        super.setDirty();
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
