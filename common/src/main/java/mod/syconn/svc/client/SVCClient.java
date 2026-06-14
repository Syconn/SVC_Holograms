package mod.syconn.svc.client;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import mod.syconn.svc.client.render.blockentity.HoloProjectorBlockEntityRenderer;
import mod.syconn.svc.client.render.item.HoloProjectorItemRenderer;
import mod.syconn.svc.client.sounds.ItemHoloProjectorSoundInstance;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.core.ModSounds;
import mod.syconn.svc.item.HoloProjectorItem;
import mod.syconn.svc.mixin.client.MinecraftAccessor;
import mod.syconn.svc.utils.interfaces.IModifiedItemRenderer;
import mod.syconn.svc.utils.interfaces.IModifiedPoseRenderer;
import mod.syconn.svc.utils.item.HologramComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class SVCClient {

    private static final Map<UUID, ItemHoloProjectorSoundInstance> SOUNDS = new HashMap<>();

    public static void init() {
        IModifiedItemRenderer.register(HoloProjectorItem.class, new HoloProjectorItemRenderer());
        IModifiedPoseRenderer.register(HoloProjectorItem.class, new HoloProjectorItemRenderer());

        ClientLifecycleEvent.CLIENT_SETUP.register(SVCClient::setupEvent);
        ClientTickEvent.CLIENT_PRE.register(SVCClient::playerTickEvent);
    }

    private static void setupEvent(Minecraft minecraft) {
        BlockEntityRendererRegistry.register(ModBlockEntities.HOLO_PROJECTOR.get(), HoloProjectorBlockEntityRenderer::new);
    }

    private static void playerTickEvent(Minecraft minecraft) {
        ClientRenderSystem.get().tick();

        var level = minecraft.level;

        if (level == null) {
            SOUNDS.values().forEach(ItemHoloProjectorSoundInstance::forceStop);
            SOUNDS.clear();
            return;
        }

        var activePlayers = new HashSet<UUID>();
        for (var player : level.players()) {
            if (!activeSound(player)) continue;
            activePlayers.add(player.getUUID());
            SOUNDS.computeIfAbsent(player.getUUID(), uuid -> {
                var sound = new ItemHoloProjectorSoundInstance(player);
                level.playLocalSound(sound.getPos(), ModSounds.HOLOGRAM_ACTIVATE.get(), SoundSource.BLOCKS, 0.3f, 1.0F, false);
                minecraft.getSoundManager().play(sound);
                return sound;
            });
        }

        Iterator<Map.Entry<UUID, ItemHoloProjectorSoundInstance>> it = SOUNDS.entrySet().iterator();
        while (it.hasNext()) {

            var entry = it.next();
            if (!activePlayers.contains(entry.getKey())) {
                level.playLocalSound(entry.getValue().getPos(), ModSounds.HOLOGRAM_DEACTIVATE.get(), SoundSource.BLOCKS, 0.3f, 1.0F, false);
                entry.getValue().forceStop();
                it.remove();
            }
        }
    }

    private static boolean activeSound(Player player) {
        for (var type : InteractionHand.values()) {
            if (player.getItemInHand(type).getItem() instanceof HoloProjectorItem) {
                var tag = HologramComponent.getOrCreate(player.getItemInHand(type));
                if ((!tag.soloRender().isEmpty() || tag.renderTarget() != null)) return true;
            }
        }
        return false;
    }

    public static float getTickDelta() {
        var mc = Minecraft.getInstance();
        if (mc.isPaused()) return ((MinecraftAccessor)mc).getPausedTickDelta();
        return mc.getFrameTimeNs();
    }
}
