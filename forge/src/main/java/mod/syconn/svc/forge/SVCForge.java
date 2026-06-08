package mod.syconn.svc.forge;

import dev.architectury.platform.forge.EventBuses;
import mod.syconn.svc.SVC;
import mod.syconn.svc.utils.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD)
public final class SVCForge {

    public SVCForge(FMLJavaModLoadingContext context) {
        EventBuses.registerModEventBus(Constants.MOD, context.getModEventBus());
        SVC.init();
    }
}
