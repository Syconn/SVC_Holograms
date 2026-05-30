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

    private final Map<UUID, HologramData> RENDERERS = new HashMap<>();

    public HoloProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(HoloProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) { // TODO ADD END CALL EFFECT
        var soloRender = this.getSoloRenderer(blockEntity.receiverUUID, blockEntity.getSoloRender());
        if (soloRender.activeRender()) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.1f, 0.5f);
            poseStack.mulPose(Axis.YN.rotationDegrees((float) blockEntity.getRotation()));
            poseStack.translate(soloRender.getInterpolatedPosition().x, soloRender.getInterpolatedPosition().y, soloRender.getInterpolatedPosition().z);
            soloRender.getRenderer().render(poseStack, buffer, partialTick, LightTexture.FULL_BLOCK);
            poseStack.popPose();
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

//    private HologramData getHologramData(UUID entity, Vec3 pos) {
//        var data = RENDERERS.get(entity);
//        if (data == null) RENDERERS.put(entity, new HologramData(entity, pos, false));
//        else if (!pos.equals(data.getCurrentPosition())) RENDERERS.put(entity, data.setPosition(pos));
//        return RENDERERS.get(entity);
//    }

    private HologramData getSoloRenderer(UUID receiverID, String playerName) {
        var data = RENDERERS.get(receiverID);
        if (data != null && data.getRenderName().equals(playerName)) return data;
        return RENDERERS.compute(receiverID, (_u, d) -> d == null ?  new HologramData(playerName) : d.generateInformation(playerName));
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
