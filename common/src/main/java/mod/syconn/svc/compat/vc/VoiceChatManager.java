package mod.syconn.svc.compat.vc;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.generic.MathUtil;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class VoiceChatManager {
    // TODO Use LocationalAudioChannel to play player audio at points, Remove
    // TODO TEST DIMENSION RENDERING

    private static final Map<UUID, Map<UUID, LocationalAudioChannel>> AUDIO_SOURCES = new HashMap<>();  // Map<CallID, Map<audioID, channel> TODO DOES THIS HAVE TO BE STATIC
    private final Map<UUID, List<UUID>> GROUP_MEMBERS = new HashMap<>(); // TODO UPDATE POSITIONS USING THIS

    public void joinCall(UUID callId, UUID callee) {
        if (VoiceChatPlugin.SERVER_API == null) return;
        GROUP_MEMBERS.computeIfAbsent(callId, id -> new ArrayList<>()).add(callee);
    }

    public void endCall(UUID callId) { // MICROPHONEPACKETEVENT
        GROUP_MEMBERS.remove(callId);
        AUDIO_SOURCES.remove(callId);
    }

    public void tick(MinecraftServer server, CallData.CallManager manager) {
        if (VoiceChatPlugin.SERVER_API == null) return;



        for (var entry : GROUP_MEMBERS.entrySet()) {
            var callId = entry.getKey();
            var call = manager.getCall(callId);
            if (call == null) continue;
            entry.getValue().removeIf(uuid -> !shouldHaveSoundDevice(server, manager, call, uuid));
        }
    }

    private boolean shouldHaveSoundDevice(MinecraftServer server, CallData.CallManager manager, CallData.Call call, UUID callee) {
        var found = false;
        for (var group : call.renderMembers.values()) {
            if (group.containsKey(callee)) {
                found = true;
//                var receiverID = call.callers.get(callee).receiverID;
//                var worldPos = manager.getBlockReceiver(receiverID).pos;
//                createOrUpdateSource(server.getLevel(worldPos.level()), call.callID, callee, worldPos.toVector().add(group.get(callee)));
            }
        }
        for (var caller : call.callers.values()) {
            if (caller.type == CallData.ReceiverType.ITEM && caller.playerUUID == callee) {
                found = true;
//                var receiverID = call.callers.get(callee).receiverID;
//                var worldPos = manager.getBlockReceiver(receiverID).pos;
//                createOrUpdateSource(server.getLevel(worldPos.level()), call.callID, callee, worldPos.toVector().add(group.get(callee)));
            }
        }
        return found;
    }

    private void createOrUpdateSource(ServerLevel serverLevel, UUID callId, UUID callee, Vec3 pos) {
        var api = VoiceChatPlugin.SERVER_API;
        if (api == null) return;

        var map = AUDIO_SOURCES.computeIfAbsent(callId, k -> new HashMap<>());
        var position = api.createPosition(pos.x, pos.y, pos.z);
        var existing = map.putIfAbsent(callee, createAudioChannel(serverLevel, callId, callee, position));
        if (existing != null) existing.updateLocation(position);
    }

    private LocationalAudioChannel createAudioChannel(ServerLevel serverLevel, UUID callId, UUID callee, Position pos) {
        var api = VoiceChatPlugin.SERVER_API;
        if (api == null) return null;

        var audioID = MathUtil.combineUUID(callId, callee);
        var channel = api.createLocationalAudioChannel(audioID, api.fromServerLevel(serverLevel), pos);
        if (channel == null) return null;

//        channel.setCategory(category); // TODO: DO I NEED?
        channel.setDistance(4);
        return channel;
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.put("group", NBTUtil.putMap(this.GROUP_MEMBERS, NBTUtil::putUUID, l -> NBTUtil.putList(l, NBTUtil::putUUID)));
        return tag;
    }

    public static VoiceChatManager load(CompoundTag tag) {
        var manager = new VoiceChatManager();
        manager.GROUP_MEMBERS.putAll(NBTUtil.getMap(tag.getCompound("group"), NBTUtil::getUUID, t -> NBTUtil.getList(t, NBTUtil::getUUID)));
        return manager;
    }
}
