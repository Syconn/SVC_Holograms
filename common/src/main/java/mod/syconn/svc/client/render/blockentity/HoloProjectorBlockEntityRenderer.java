package mod.syconn.svc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.blockentity.HoloProjectorBlockEntity;
import mod.syconn.svc.utils.client.HologramData;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HoloProjectorBlockEntityRenderer implements BlockEntityRenderer<HoloProjectorBlockEntity> {

//    private final Map<UUID, HologramData> RENDERERS = new HashMap<>();

    public HoloProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(HoloProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
//        for (var renderable : blockEntity.getRenderables().entrySet()) {
//            poseStack.pushPose();
//
//            var hologramData = getHologramData(renderable.getKey(), renderable.getValue());
//            poseStack.translate(hologramData.getInterpolatedPosition().x, hologramData.getInterpolatedPosition().y, hologramData.getInterpolatedPosition().z);
//            hologramData.getRenderer().render(poseStack, buffer, partialTick, LightTexture.FULL_BLOCK);
//
//            poseStack.popPose();
//        }

//        for (var removed : blockEntity.getDeletions().entrySet()) { // TODO CONCUREENT
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

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public boolean shouldRenderOffScreen(HoloProjectorBlockEntity blockEntity) {
        return false; // !blockEntity.getRenderables().isEmpty() && shouldRender(blockEntity, GameInstance.getClient().getCameraEntity().getEyePosition());
    }
}
