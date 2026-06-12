package mod.syconn.svc.compat;

import dev.architectury.platform.Platform;

public class CompatManager {

    public static boolean hasSVC() {
        return Platform.isModLoaded("voicechat");
    }
}
