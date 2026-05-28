package mod.syconn.svc.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.client.SVCClient;
import mod.syconn.svc.utils.client.HologramData;
import mod.syconn.svc.utils.generic.MathUtil;
import mod.syconn.svc.utils.generic.ModelUtil;
import mod.syconn.svc.utils.interfaces.IModifiedItemRenderer;
import mod.syconn.svc.utils.interfaces.IModifiedPoseRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HoloProjectorItemRenderer implements IModifiedItemRenderer, IModifiedPoseRenderer {

    private final Map<UUID, HologramData> RENDERERS = new HashMap<>();

    @Override
    public boolean render(LivingEntity entity, ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay, BakedModel model) {
        poseStack.pushPose();

        model.getTransforms().getTransform(renderMode).apply(leftHanded, poseStack);
        if (renderMode != ItemDisplayContext.GUI) renderDirect(stack, renderMode, poseStack, bufferSource);

        poseStack.popPose();
        return false;
    }


    private void renderDirect(ItemStack stack, ItemDisplayContext renderMode, PoseStack poseStack, MultiBufferSource bufferSource) {
        var holo = HologramData.HologramTag.getOrCreate(stack);
        var hologramData = getHologramData(holo);

        if (hologramData != null) {
            poseStack.pushPose();

            poseStack.translate(hologramData.getCurrentPosition().x, hologramData.getCurrentPosition().y, hologramData.getCurrentPosition().z);
            poseStack.mulPose(Axis.YN.rotationDegrees(ModelUtil.isLeftHanded(renderMode) ? -45f : 45f));
            poseStack.scale(0.6f, 0.6f, 0.6f);

            hologramData.getRenderer().render(poseStack, bufferSource, SVCClient.getTickDelta(), LightTexture.FULL_BLOCK);

            poseStack.popPose();
        }
    }

    private HologramData getHologramData(HologramData.HologramTag hologramTag) {
        var data = RENDERERS.get(hologramTag.itemId);
        if (data == null && hologramTag.uuid != null) RENDERERS.put(hologramTag.itemId, new HologramData(hologramTag.uuid, new Vec3(0f, -0.43f, 0f), true));
        else if (data != null) {
            if (data.getTransition() == 0 && hologramTag.uuid == null) data.endCall(() -> RENDERERS.remove(hologramTag.itemId));
            else if (hologramTag.uuid != null && !hologramTag.uuid.equals(data.getPlayer().getUUID())) RENDERERS.put(hologramTag.itemId, new HologramData(hologramTag.uuid, new Vec3(0f, -0.43f, 0f), true));
        }
        return RENDERERS.get(hologramTag.itemId);
    }

    @Override
    public void modifyPose(LivingEntity entity, InteractionHand hand, ItemStack stack, HumanoidModel<? extends LivingEntity> model, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float tickDelta) {
        var mc = GameInstance.getClient();
        if (mc.player == entity && mc.options.getCameraType().isFirstPerson()) return;

        if (HologramData.HologramTag.getOrCreate(stack).uuid != null)
            ModelUtil.smartLerpArmsRadians(entity, hand, model, 1, 0, 0, 0, MathUtil.toRadians(-145), 0, 0);
    }
}
