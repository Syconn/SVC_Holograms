package mod.syconn.svc.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.client.SVCClient;
import mod.syconn.svc.client.model.HologramModel;
import mod.syconn.svc.utils.client.HologramData;
import mod.syconn.svc.utils.generic.MathUtil;
import mod.syconn.svc.utils.generic.ModelUtil;
import mod.syconn.svc.utils.interfaces.IModifiedItemRenderer;
import mod.syconn.svc.utils.interfaces.IModifiedPoseRenderer;
import mod.syconn.svc.utils.item.HologramTag;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class HoloProjectorItemRenderer implements IModifiedItemRenderer, IModifiedPoseRenderer {

    private final Map<UUID, HologramData> RENDERER = new HashMap<>();
    private float spin = 0;

    @Override
    public boolean render(LivingEntity entity, HumanoidModel<? extends LivingEntity> playerModel, ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay, BakedModel model) {
        if (playerModel instanceof HologramModel) return false;

        poseStack.pushPose();
        model.getTransforms().getTransform(renderMode).apply(leftHanded, poseStack);
        if (renderMode != ItemDisplayContext.GUI) renderDirect(stack, renderMode, poseStack, bufferSource);
        poseStack.popPose();
        return false;
    }

    private void renderDirect(ItemStack stack, ItemDisplayContext renderMode, PoseStack poseStack, MultiBufferSource bufferSource) {
        var tag = HologramTag.getOrCreate(stack);
        var hologramData = getHologramData(tag);
        if (!tag.getSoloRender().isEmpty()) this.spin += 0.25f;

        if (hologramData.activeRender()) { // TODO Item Sounds, SVC, Server Testing, Forge Testing
            poseStack.pushPose();
            poseStack.mulPose(Axis.YN.rotationDegrees(-hologramData.getPlayer().getYRot()));
            poseStack.mulPose(Axis.YN.rotationDegrees(ModelUtil.isLeftHanded(renderMode) ? -45f : 45f));
            if (tag.getRenderTarget() == null) poseStack.mulPose(Axis.YN.rotationDegrees(spin));
            poseStack.translate(0f, -0.4f, 0f);
            poseStack.scale(0.6f, 0.6f, 0.6f);
            hologramData.getRenderer().render(poseStack, bufferSource, SVCClient.getTickDelta(), LightTexture.FULL_BLOCK);
            poseStack.popPose();
        } else RENDERER.remove(tag.getReceiverID());
    }

    private HologramData getHologramData(HologramTag tag) {
        return RENDERER.compute(tag.getReceiverID(), (_u, d) -> {
            if (d != null && ((Objects.equals(d.getRenderName(), tag.getSoloRender()) && !tag.getSoloRender().isEmpty()) || (d.getPlayerID() != null && d.getPlayerID().equals(tag.getRenderTarget())))) return d;
            if (tag.getRenderTarget() != null) return new HologramData(tag.getRenderTarget());
            return d == null ? new HologramData(tag.getSoloRender()) : d.generateInformationByName(tag.getSoloRender());
        });
    }

    @Override
    public void modifyPose(LivingEntity entity, InteractionHand hand, ItemStack stack, HumanoidModel<? extends LivingEntity> model, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float tickDelta) {
        var mc = GameInstance.getClient();
        var tag = HologramTag.getOrCreate(stack);

        if (model instanceof HologramModel) return;
        if (mc.player == entity && mc.options.getCameraType().isFirstPerson()) return;
        if (!tag.getSoloRender().isEmpty() || tag.getRenderTarget() != null) ModelUtil.smartLerpArmsRadians(entity, hand, model, 1, 0, 0, 0, MathUtil.toRadians(-145), 0, 0);
    }
}
