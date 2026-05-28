package mod.syconn.svc.client;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import mod.syconn.svc.client.render.blockentity.HoloProjectorBlockEntityRenderer;
import mod.syconn.svc.client.render.item.HoloProjectorItemRenderer;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.item.HoloProjectorItem;
import mod.syconn.svc.mixin.client.MinecraftAccessor;
import mod.syconn.svc.utils.interfaces.IModifiedItemRenderer;
import mod.syconn.svc.utils.interfaces.IModifiedPoseRenderer;
import net.minecraft.client.Minecraft;

public class SVCClient {

    public static void init() {
        IModifiedItemRenderer.register(HoloProjectorItem.class, new HoloProjectorItemRenderer());
        IModifiedPoseRenderer.register(HoloProjectorItem.class, new HoloProjectorItemRenderer());

        ClientLifecycleEvent.CLIENT_SETUP.register(SVCClient::setupEvent);
    }

    public static void setupEvent(Minecraft minecraft) {
        BlockEntityRendererRegistry.register(ModBlockEntities.HOLO_PROJECTOR.get(), HoloProjectorBlockEntityRenderer::new);
    }

    public static float getTickDelta() {
        var mc = Minecraft.getInstance();
        if (mc.isPaused()) return ((MinecraftAccessor)mc).getPausedTickDelta();
        return mc.getFrameTime();
    }
}
