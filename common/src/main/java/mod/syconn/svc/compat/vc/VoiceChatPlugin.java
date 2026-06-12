package mod.syconn.svc.compat.vc;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import mod.syconn.svc.utils.Constants;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@ForgeVoicechatPlugin
public class VoiceChatPlugin implements VoicechatPlugin {

    @Nullable
    public static VoicechatServerApi SERVER_API;

    @Override
    public String getPluginId() {
        return Constants.MOD;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        SERVER_API = event.getVoicechat();
    }

    public static Group creeteGroup(UUID callId) {
        if (VoiceChatPlugin.SERVER_API == null) return null;
        return VoiceChatPlugin.SERVER_API.groupBuilder().setPersistent(true).setName("HoloCall:" + callId).setHidden(true).setPassword(callId.toString()).setType(Group.Type.OPEN).build();
    }
}
