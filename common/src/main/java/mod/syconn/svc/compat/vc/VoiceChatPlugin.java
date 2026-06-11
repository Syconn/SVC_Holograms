package mod.syconn.svc.compat.vc;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.utils.Constants;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@ForgeVoicechatPlugin
public class VoiceChatPlugin implements VoicechatPlugin {

    @Nullable
    public static VoicechatServerApi SERVER_API;

    public static LocationalAudioChannel channel;

    @Override
    public String getPluginId() {
        return Constants.MOD;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(MicrophonePacketEvent.class, this::onPacketReceived);
    }

    private void onPacketReceived(MicrophonePacketEvent microphonePacketEvent) {
        if (SERVER_API == null) return;

        if (channel == null) {
            channel = SERVER_API.createLocationalAudioChannel(UUID.randomUUID(), SERVER_API.fromServerLevel(GameInstance.getServer().overworld()), SERVER_API.createPosition(0, 100, 0));
            if (channel != null) channel.setDistance(100);
        }

        if (channel != null) channel.send(microphonePacketEvent.getPacket());
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        SERVER_API = event.getVoicechat();
    }
}
