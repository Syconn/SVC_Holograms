package mod.syconn.svc.forge;

import mod.syconn.svc.SVC;
import mod.syconn.svc.utils.Constants;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD)
public final class SVCForge {
    public SVCForge() {
        SVC.init();
    }
}
