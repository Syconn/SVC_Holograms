package mod.syconn.svc.compat.vc;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import mod.syconn.svc.utils.Constants;

public class VoiceChatPlugin implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return Constants.MOD;
    }

    @Override
    public void initialize(VoicechatApi api) {

    }
}
