package mod.syconn.svc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.blockentity.HoloProjectorBlockEntity;
import mod.syconn.svc.utils.client.HologramData;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HoloProjectorBlockEntityRenderer implements BlockEntityRenderer<HoloProjectorBlockEntity> {

    private final Map<UUID, HologramData> SOLO_RENDERER = new HashMap<>();
    private final Map<UUID, Map<UUID, HologramData>> MULI_RENDERER = new HashMap<>();

    public HoloProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(HoloProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) { // TODO ADD END CALL EFFECT
        if (!blockEntity.getSoloRender().isEmpty()) {
            var soloRenderer = this.getSoloRenderer(blockEntity.getReceiverUUID(), blockEntity.getSoloRender());
            if (soloRenderer.activeRender()) {
                poseStack.pushPose();
                poseStack.translate(0.5f, 0.1f, 0.5f);
                poseStack.mulPose(Axis.YN.rotationDegrees((float) blockEntity.getRotation()));
                poseStack.translate(soloRenderer.getInterpolatedPosition().x, soloRenderer.getInterpolatedPosition().y, soloRenderer.getInterpolatedPosition().z);
                soloRenderer.getRenderer().render(poseStack, buffer, partialTick, LightTexture.FULL_BLOCK);
                poseStack.popPose();
            }
        } else {
            blockEntity.getRenderables().forEach(uuid -> this.getMultiplayerRenderer(blockEntity.getReceiverUUID(), uuid, true));

            for (var entry : getMulitplayerMap(blockEntity.getReceiverUUID()).entrySet()) {
                var multiRender = this.getMultiplayerRenderer(blockEntity.getReceiverUUID(), entry.getKey());
                if (multiRender.activeRender()) { // && blockEntity.getRenderables().contains(multiRender.getPlayerID())
                    poseStack.pushPose();
                    poseStack.translate(0.5f, 0.1f, 0.5f);
                    poseStack.translate(multiRender.getInterpolatedPosition().x, multiRender.getInterpolatedPosition().y, multiRender.getInterpolatedPosition().z);
                    multiRender.getRenderer().render(poseStack, buffer, partialTick, LightTexture.FULL_BLOCK);
                    poseStack.popPose();
                }

                if (!blockEntity.getRenderables().contains(multiRender.getPlayerID())) multiRender.endCall();
            }

//            var multiRender = this.getMultiplayerRenderer(blockEntity.getReceiverUUID(), renderable);
//            if (multiRender.activeRender()) {
//                poseStack.pushPose();
//                poseStack.translate(0.5f, 0.1f, 0.5f);
//                poseStack.translate(multiRender.getInterpolatedPosition().x, multiRender.getInterpolatedPosition().y, multiRender.getInterpolatedPosition().z);
//                multiRender.getRenderer().render(poseStack, buffer, partialTick, LightTexture.FULL_BLOCK);
//                poseStack.popPose();
//            }

//            for (var renderable : blockEntity.getRenderables()) {
//
//            }
        }

//        for (var removed : blockEntity.getDeletions().entrySet()) { // TODO CONCURRENT
//            poseStack.pushPose();
//
//            var data = RENDERERS.get(removed.getKey());
//            if (data != null) {
//                poseStack.translate(data.getInterpolatedPosition().x, data.getInterpolatedPosition().y, data.getInterpolatedPosition().z);
//                data.getRenderer().render(poseStack, buffer, partialTick, LightTexture.FULL_BLOCK);
//                data.endCall(() -> {
//                    blockEntity.removeDeletion(removed.getKey());
//                    this.RENDERERS.remove(removed.getKey());
//                });
//            }
//
//            poseStack.popPose();
//        }
    }

    private HologramData getSoloRenderer(UUID receiverID, String playerName) {
        var data = SOLO_RENDERER.get(receiverID);
        if (data != null && data.getRenderName().equals(playerName)) return data;
        return SOLO_RENDERER.compute(receiverID, (_u, d) -> d == null ?  new HologramData(playerName) : d.generateInformationByName(playerName));
    }

    private HologramData getMultiplayerRenderer(UUID receiverID, UUID playerID) {
        return MULI_RENDERER.computeIfAbsent(receiverID, u -> new HashMap<>()).computeIfAbsent(playerID, HologramData::new);
    }

    private HologramData getMultiplayerRenderer(UUID receiverID, UUID playerID, boolean render) {
        return MULI_RENDERER.computeIfAbsent(receiverID, u -> new HashMap<>()).compute(playerID, (uuid, hologramData) -> hologramData == null ? new HologramData(uuid) : hologramData.setActiveRender(render));
    }

    private Map<UUID, HologramData> getMulitplayerMap(UUID receiverID) {
        return MULI_RENDERER.computeIfAbsent(receiverID, u -> new HashMap<>());
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public boolean shouldRenderOffScreen(HoloProjectorBlockEntity blockEntity) {
        if (GameInstance.getClient().getCameraEntity() == null) return false;
        return shouldRender(blockEntity, GameInstance.getClient().getCameraEntity().getEyePosition()); // !blockEntity.getRenderables().isEmpty() && shouldRender(blockEntity, GameInstance.getClient().getCameraEntity().getEyePosition());
    }
}
