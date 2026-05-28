package mod.syconn.svc;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import mod.syconn.svc.client.SVCClient;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.core.ModBlocks;
import mod.syconn.svc.core.ModItems;
import mod.syconn.svc.core.ModSounds;
import mod.syconn.svc.network.Network;
import mod.syconn.svc.server.SVCServer;

public final class SVC {

    public static void init() {
        ModBlocks.BLOCKS.register();
        ModItems.ITEMS.register();
        ModItems.TABS.register();
        ModSounds.SOUNDS.register();
        ModBlockEntities.BLOCK_ENTITIES.register();

        Network.init();

        EnvExecutor.runInEnv(Env.CLIENT, () -> SVCClient::init);
        LifecycleEvent.SETUP.register(SVCServer::init);
    }
}
