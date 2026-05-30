package mod.syconn.svc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.utils.client.HologramData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HologramRenderer extends PlayerRenderer {

    private static final Minecraft mc = GameInstance.getClient();
    private final HologramData data;

    public HologramRenderer(HologramData data, boolean useSlimModel) {
        super(new EntityRendererProvider.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderer(), mc.getEntityRenderDispatcher().getItemInHandRenderer(),
                mc.getResourceManager(), mc.getEntityModels(), mc.font), useSlimModel);
        this.data = data;
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, float partialTicks, int packedLight) {
        data.tick();

        if (this.data.shouldRender()) {
            var player = this.data.getPlayer();

            poseStack.pushPose();
//            var scale = this.data.getAnimationScale(partialTicks); TODO ADD BACK
//            poseStack.scale(scale, scale, scale);
            if (this.data.isStaticRender()) this.setModelProperties(player);
            this.render(player, 0, partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    @Override
    protected @Nullable RenderType getRenderType(AbstractClientPlayer livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(this.data.getSkin());
//        return this.data.isItem() ? RenderType.itemEntityTranslucentCull(this.getTextureLocation(livingEntity)) : RenderType.entityTranslucentCull(this.getTextureLocation(livingEntity));
    }

    @Override
    protected void renderNameTag(AbstractClientPlayer entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    private void setModelProperties(AbstractClientPlayer pClientPlayer) {
        PlayerModel<AbstractClientPlayer> playermodel = this.getModel();
        playermodel.setAllVisible(true);
        playermodel.rightPants.visible = true;
        playermodel.leftPants.visible = true;
        playermodel.leftSleeve.visible = true;
        playermodel.rightSleeve.visible = true;
        playermodel.jacket.visible = true;
        playermodel.hat.visible = true;
        pClientPlayer.setYHeadRot(0);
    }

    public void render(AbstractClientPlayer entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        this.model.attackTime = this.getAttackAnim(entity, partialTicks);
        this.model.riding = entity.isPassenger();
        this.model.young = entity.isBaby();
        float f = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float g = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float h = g - f;
        if (entity.isPassenger() && entity.getVehicle() instanceof LivingEntity livingEntity) {
            f = Mth.rotLerp(partialTicks, livingEntity.yBodyRotO, livingEntity.yBodyRot);
            h = g - f;
            float i = Mth.wrapDegrees(h);
            if (i < -85.0F) i = -85.0F;
            if (i >= 85.0F) i = 85.0F;
            f = g - i;
            if (i * i > 2500.0F) f += i * 0.2F;
            h = g - f;
        }

        float j = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        if (isEntityUpsideDown(entity)) {
            j *= -1.0F;
            h *= -1.0F;
        }

        if (entity.hasPose(Pose.SLEEPING)) {
            Direction direction = entity.getBedOrientation();
            if (direction != null) {
                float k = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                poseStack.translate(-direction.getStepX() * k, 0.0F, -direction.getStepZ() * k);
            }
        }

        float ix = this.getBob(entity, partialTicks);
        this.setupRotations(entity, poseStack, ix, f, partialTicks);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(entity, poseStack, partialTicks);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        float k = 0.0F;
        float l = 0.0F;
        if (!entity.isPassenger() && entity.isAlive()) {
            k = entity.walkAnimation.speed(partialTicks);
            l = entity.walkAnimation.position(partialTicks);
            if (k > 1.0F) k = 1.0F;
        }

        this.model.prepareMobModel(entity, l, k, partialTicks);
        this.model.setupAnim(entity, l, k, ix, h, j);
        Minecraft minecraft = Minecraft.getInstance();
        boolean bl = this.isBodyVisible(entity);
        boolean bl2 = !bl && !entity.isInvisibleTo(minecraft.player);
        boolean bl3 = minecraft.shouldEntityAppearGlowing(entity);
        RenderType renderType = this.getRenderType(entity, bl, bl2, bl3);
        if (renderType != null) {
            VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
            int m = getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks));
            this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, m, 1.0F, 1.0F, 1.0F, bl2 ? 0.15F : 1.0F);
        }

        if (!entity.isSpectator()) {
            for (var renderLayer : this.layers) renderLayer.render(poseStack, buffer, packedLight, entity, l, k, partialTicks, ix, h, j);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
