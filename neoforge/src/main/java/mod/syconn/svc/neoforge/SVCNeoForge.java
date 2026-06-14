package mod.syconn.svc.neoforge;

import mod.syconn.svc.SVC;
import mod.syconn.svc.utils.Constants;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD)
public final class SVCNeoForge {

    public SVCNeoForge() {
        SVC.init();
    }
}
