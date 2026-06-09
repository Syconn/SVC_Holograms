package mod.syconn.svc.compat.vc;

import de.maxhenkel.voicechat.api.Group;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

public class VoiceChatManager {
    // TODO Use LocationalAudioChannel to play player audio at points

    private final Map<UUID, UUID> PERSISTENT_GROUPS = new HashMap<>();
    private final Map<UUID, List<UUID>> GROUP_MEMBERS = new HashMap<>();

    public void createBlockCall(UUID callId) {
        if (VoiceChatPlugin.SERVER_API == null) return;

        var group = VoiceChatPlugin.SERVER_API.groupBuilder().setPersistent(true).setName("HoloCall:" + callId).setHidden(true).setPassword(callId.toString()).setType(Group.Type.OPEN).build();
        this.PERSISTENT_GROUPS.put(callId, group.getId());
    }

    public void removeBlockCall(UUID callId) {
        if (VoiceChatPlugin.SERVER_API == null || !this.PERSISTENT_GROUPS.containsKey(callId)) return;

        VoiceChatPlugin.SERVER_API.removeGroup(this.PERSISTENT_GROUPS.get(callId));
        this.PERSISTENT_GROUPS.remove(callId);
    }

    public void joinBlockCall(UUID callId, CallData.Callee callee) { // TODO RN THIS IS ONLY WORKS IF PLAYER JOINS AND LEAVES NOT IF THEY ARE IN COMMUNICATION RANGE
        if (VoiceChatPlugin.SERVER_API == null || !this.PERSISTENT_GROUPS.containsKey(callId)) return;

        var connection = VoiceChatPlugin.SERVER_API.getConnectionOf(callee.playerUUID);
        var group = VoiceChatPlugin.SERVER_API.getGroup(this.PERSISTENT_GROUPS.get(callId));
        if (connection == null) return;
        connection.setGroup(group);
        GROUP_MEMBERS.computeIfAbsent(callId, id -> new ArrayList<>()).add(callee.playerUUID);
    }

    public void leaveBlockCall(CallData.Callee callee) {
        if (VoiceChatPlugin.SERVER_API == null) return;

        var connection = VoiceChatPlugin.SERVER_API.getConnectionOf(callee.playerUUID);
        if (connection == null || connection.getGroup() == null) return;
        connection.setGroup(null);
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
