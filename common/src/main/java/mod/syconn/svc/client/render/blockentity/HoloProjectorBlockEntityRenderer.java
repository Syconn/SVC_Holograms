package mod.syconn.svc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.blockentity.HoloProjectorBlockEntity;
import mod.syconn.svc.utils.client.HologramData;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class HoloProjectorBlockEntityRenderer implements BlockEntityRenderer<HoloProjectorBlockEntity> {

//    private final Map<UUID, HologramData> RENDERERS = new HashMap<>();
    private HologramData test = null;

    public HoloProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(HoloProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (this.test != null) {
            poseStack.pushPose();
            var hologramData = test;
            poseStack.translate(0.5f, 0f, 0.5f);
//            poseStack.translate(hologramData.getInterpolatedPosition().x, hologramData.getInterpolatedPosition().y, hologramData.getInterpolatedPosition().z);
            hologramData.getRenderer().render(poseStack, buffer, partialTick, LightTexture.FULL_BLOCK);
            poseStack.popPose();
        } else {
            var player = GameInstance.getClient().player;
            if (player == null) return;
            test = new HologramData("Syconn");
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

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public boolean shouldRenderOffScreen(HoloProjectorBlockEntity blockEntity) {
        return false; // !blockEntity.getRenderables().isEmpty() && shouldRender(blockEntity, GameInstance.getClient().getCameraEntity().getEyePosition());
    }
}
