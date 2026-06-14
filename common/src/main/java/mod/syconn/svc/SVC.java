package mod.syconn.svc;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import mod.syconn.svc.client.SVCClient;
import mod.syconn.svc.core.*;
import mod.syconn.svc.network.Network;
import mod.syconn.svc.server.SVCServer;
import mod.syconn.svc.utils.entity.ExtraRenderInfoManager;

public final class SVC {

    public static void init() {
        ModComponents.DATA_COMPONENTS.register();
        ModBlocks.BLOCKS.register();
        ModItems.ITEMS.register();
        ModItems.TABS.register();
        ModSounds.SOUNDS.register();
        ModBlockEntities.BLOCK_ENTITIES.register();

        Network.init();
        ExtraRenderInfoManager.register();

        EnvExecutor.runInEnv(Env.CLIENT, () -> SVCClient::init);
        LifecycleEvent.SETUP.register(SVCServer::init);
    }
}
