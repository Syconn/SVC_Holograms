package mod.syconn.svc.compat.vc;

import mod.syconn.svc.server.savedData.extra.CallData;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public interface IVoiceChatManager {

    void createCall(UUID callId);

    void joinCall(UUID callId, UUID callee);

    void endCall(UUID callId);

    void tick(CallData.CallManager manager);

    CompoundTag save();
}
