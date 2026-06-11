package mod.syconn.svc.compat.vc;

import de.maxhenkel.voicechat.api.Group;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class VoiceChatManager {
    // TODO Use LocationalAudioChannel to play player audio at points, Test check forge
    // TODO TEST INDEPENDENT SERVER

    private final Map<UUID, UUID> PERSISTENT_GROUPS = new HashMap<>();
    private final Map<UUID, List<UUID>> GROUP_MEMBERS = new HashMap<>();

    public void createCall(UUID callId) {
        if (VoiceChatPlugin.SERVER_API == null) return;

        var group = VoiceChatPlugin.SERVER_API.groupBuilder().setPersistent(true).setName("HoloCall:" + callId).setHidden(true).setPassword(callId.toString()).setType(Group.Type.OPEN).build();
        this.PERSISTENT_GROUPS.put(callId, group.getId());
    }

    public void removeCall(UUID callId) {
        if (VoiceChatPlugin.SERVER_API == null || !this.PERSISTENT_GROUPS.containsKey(callId)) return;

        VoiceChatPlugin.SERVER_API.removeGroup(this.PERSISTENT_GROUPS.get(callId));
        this.PERSISTENT_GROUPS.remove(callId);
    }

    public void joinCall(UUID callId, UUID callee) {
        if (VoiceChatPlugin.SERVER_API == null || !this.PERSISTENT_GROUPS.containsKey(callId)) return;

        var connection = VoiceChatPlugin.SERVER_API.getConnectionOf(callee);
        var group = VoiceChatPlugin.SERVER_API.getGroup(this.PERSISTENT_GROUPS.get(callId));
        if (group == null) {
            System.out.println("Missing VC group for call " + callId);
            return;
        }

        if (connection == null || (connection.getGroup() != null && connection.getGroup().getId().equals(this.PERSISTENT_GROUPS.get(callId)))) return;
        connection.setGroup(group);
        GROUP_MEMBERS.computeIfAbsent(callId, id -> new ArrayList<>()).add(callee);
    }

    public void endCall(UUID callId) {
        var members = GROUP_MEMBERS.remove(callId);
        if (members != null) for (UUID member : members) leaveCallSafe(member);

        removeCall(callId);
    }

    public void tick(CallData.CallManager manager) {
        for (var entry : GROUP_MEMBERS.entrySet()) {
            var callId = entry.getKey();
            var call = manager.getCall(callId);
            if (call == null) continue;
            for (Iterator<UUID> it = entry.getValue().iterator(); it.hasNext(); ) {
                var callee = it.next();
                if (!isInAnyRenderGroup(call.renderMembers, callee) && !isHandheldCaller(call.callers, callee)) {
                    it.remove();
                    leaveCallSafe(callee);
                }
            }
        }
    }

    private boolean isInAnyRenderGroup(Map<UUID, Map<UUID, Vec3>> renderMembers, UUID callee) {
        for (var group : renderMembers.values())
            if (group.containsKey(callee)) return true;
        return false;
    }

    private boolean isHandheldCaller(Map<UUID, CallData.Callee> callers, UUID callee) {
        for (var caller : callers.values())
            if (caller.type == CallData.ReceiverType.ITEM && caller.playerUUID == callee) return true;
        return false;
    }

    private void leaveCallSafe(UUID callee) {
        if (VoiceChatPlugin.SERVER_API == null) return;

        var connection = VoiceChatPlugin.SERVER_API.getConnectionOf(callee);
        if (connection != null && connection.getGroup() != null) connection.setGroup(null);
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.put("persistent", NBTUtil.putMap(this.PERSISTENT_GROUPS, NBTUtil::putUUID, NBTUtil::putUUID));
        tag.put("group", NBTUtil.putMap(this.GROUP_MEMBERS, NBTUtil::putUUID, l -> NBTUtil.putList(l, NBTUtil::putUUID)));
        return tag;
    }

    public static VoiceChatManager load(CompoundTag tag) {
        var manager = new VoiceChatManager();
        manager.PERSISTENT_GROUPS.putAll(NBTUtil.getMap(tag.getCompound("persistent"), NBTUtil::getUUID, NBTUtil::getUUID));
        manager.GROUP_MEMBERS.putAll(NBTUtil.getMap(tag.getCompound("group"), NBTUtil::getUUID, t -> NBTUtil.getList(t, NBTUtil::getUUID)));
        return manager;
    }
}
