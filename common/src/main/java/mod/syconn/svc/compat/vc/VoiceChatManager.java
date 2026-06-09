package mod.syconn.svc.compat.vc;

import de.maxhenkel.voicechat.api.Group;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoiceChatManager {

    private final Map<UUID, UUID> PERSISTENT_GROUPS = new HashMap<>();

    public void createBlockCall(UUID callId) { // TODO AUTO DO ON SERVER START
        if (VoiceChatPlugin.SERVER_API == null) return;
        var group = VoiceChatPlugin.SERVER_API.groupBuilder().setPersistent(true).setName("HoloCall:" + callId).setHidden(false).setPassword(callId.toString()).setType(Group.Type.OPEN).build();
        this.PERSISTENT_GROUPS.put(callId, group.getId());
    }

    public void removeBlockCall(UUID callId) {
        if (VoiceChatPlugin.SERVER_API == null || !this.PERSISTENT_GROUPS.containsKey(callId)) return;
        VoiceChatPlugin.SERVER_API.removeGroup(this.PERSISTENT_GROUPS.get(callId));
        this.PERSISTENT_GROUPS.remove(callId);
    }

    public void joinBlockCall(UUID callId, CallData.Callee callee) {
        if (VoiceChatPlugin.SERVER_API == null || !this.PERSISTENT_GROUPS.containsKey(callId)) return;

        var connection = VoiceChatPlugin.SERVER_API.getConnectionOf(callee.playerUUID);
        var group = VoiceChatPlugin.SERVER_API.getGroup(this.PERSISTENT_GROUPS.get(callId));
        if (connection == null || connection.getGroup() != null) return;
        connection.setGroup(group);
    }

    public void leaveBlockCall(UUID callId, CallData.Callee callee) {
        if (VoiceChatPlugin.SERVER_API == null || !this.PERSISTENT_GROUPS.containsKey(callId)) return;

        var connection = VoiceChatPlugin.SERVER_API.getConnectionOf(callee.playerUUID);
        var group = VoiceChatPlugin.SERVER_API.getGroup(this.PERSISTENT_GROUPS.get(callId));
        if (connection == null || connection.getGroup() != null) return;
        connection.setGroup(group);
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.put("persistent", NBTUtil.putMap(this.PERSISTENT_GROUPS, NBTUtil::putUUID, NBTUtil::putUUID));
        return tag;
    }

    public static VoiceChatManager load(CompoundTag tag) {
        var manager = new VoiceChatManager();
        manager.PERSISTENT_GROUPS.putAll(NBTUtil.getMap(tag.getCompound("persistent"), NBTUtil::getUUID, NBTUtil::getUUID));
        return manager;
    }
}
