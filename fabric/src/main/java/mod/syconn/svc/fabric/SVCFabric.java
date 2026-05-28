package mod.syconn.svc.fabric;

import mod.syconn.svc.SVC;
import net.fabricmc.api.ModInitializer;

public final class SVCFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SVC.init();
    }
}
