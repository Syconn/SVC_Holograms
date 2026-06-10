package mod.syconn.svc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.client.model.HologramModel;
import mod.syconn.svc.utils.client.HologramData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class HologramRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final Minecraft mc = GameInstance.getClient();
    private final HologramData data;

    private HologramRenderer(EntityRendererProvider.Context context, HologramData data, boolean useSlimModel) {
        super(context, new HologramModel(context.bakeLayer(useSlimModel ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), useSlimModel), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel<>(context.bakeLayer(useSlimModel ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(context.bakeLayer(useSlimModel ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
        this.addLayer(new PlayerItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new ArrowLayer<>(context, this));
        this.addLayer(new Deadmau5EarsLayer(this));
        this.addLayer(new CapeLayer(this));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
        this.addLayer(new ParrotOnShoulderLayer<>(this, context.getModelSet()));
        this.addLayer(new SpinAttackEffectLayer<>(this, context.getModelSet()));
        this.addLayer(new BeeStingerLayer<>(this));
        this.data = data;
    }

    public HologramRenderer(HologramData data, boolean useSlimModel) {
        this(new EntityRendererProvider.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderer(), mc.getEntityRenderDispatcher().getItemInHandRenderer(), mc.getResourceManager(), mc.getEntityModels(), mc.font),
                data, useSlimModel);
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, float partialTicks, int packedLight) {
        data.tick();

        if (this.data.shouldRender()) {
            var player = this.data.getPlayer();
            poseStack.pushPose();
            var scale = this.data.getAnimationScale(partialTicks);
            poseStack.scale(scale, scale, scale);
            if (this.data.isStaticRender()) this.setModelProperties(player);
            this.render(player, partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    @Override
    protected @Nullable RenderType getRenderType(AbstractClientPlayer livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(this.data.getSkin() == null ? DefaultPlayerSkin.getDefaultSkin() : this.data.getSkin());
    }

    @Override
    protected void renderNameTag(AbstractClientPlayer entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) { }

    private void setModelProperties(AbstractClientPlayer pClientPlayer) {
        HologramModel hologramModel = (HologramModel) this.getModel();
        hologramModel.setAllVisible(true);
        hologramModel.rightPants.visible = true;
        hologramModel.leftPants.visible = true;
        hologramModel.leftSleeve.visible = true;
        hologramModel.rightSleeve.visible = true;
        hologramModel.jacket.visible = true;
        hologramModel.hat.visible = true;
        pClientPlayer.setYHeadRot(0);
    }

    public void render(AbstractClientPlayer player, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        this.model.attackTime = this.getAttackAnim(player, partialTicks);
        this.model.riding = player.isPassenger();
        this.model.young = player.isBaby();
        var f = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        var g = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot);
        var h = g - f;
        if (player.isPassenger() && player.getVehicle() instanceof LivingEntity livingEntity) {
            f = Mth.rotLerp(partialTicks, livingEntity.yBodyRotO, livingEntity.yBodyRot);
            h = g - f;
            var i = Mth.wrapDegrees(h);
            if (i < -85.0F) i = -85.0F;
            if (i >= 85.0F) i = 85.0F;
            f = g - i;
            if (i * i > 2500.0F) f += i * 0.2F;
            h = g - f;
        }

        var j = Mth.lerp(partialTicks, player.xRotO, player.getXRot());
        if (isEntityUpsideDown(player)) {
            j *= -1.0F;
            h *= -1.0F;
        }

        var ix = this.getBob(player, partialTicks);
        this.setupRotations(player, poseStack, ix, f, partialTicks);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(player, poseStack, partialTicks);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        var k = 0.0F;
        var l = 0.0F;
        if (!player.isPassenger() && player.isAlive()) {
            k = player.walkAnimation.speed(partialTicks);
            l = player.walkAnimation.position(partialTicks);
            if (k > 1.0F) k = 1.0F;
        }

//        System.out.println(k);

        this.model.prepareMobModel(player, l, k, partialTicks);
        this.model.setupAnim(player, l, k, ix, h, j);
        var minecraft = Minecraft.getInstance();
        var bl = this.isBodyVisible(player);
        var bl2 = !bl && !player.isInvisibleTo(minecraft.player);
        var bl3 = minecraft.shouldEntityAppearGlowing(player);
        var renderType = this.getRenderType(player, bl, bl2, bl3);

        if (renderType != null) {
            var vertexConsumer = buffer.getBuffer(renderType);
            int m = getOverlayCoords(player, this.getWhiteOverlayProgress(player, partialTicks));
            this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, m, 1.0F, 1.0F, 1.0F, bl2 ? 0.15F : 1.0F);
        }

        if (!player.isSpectator()) for (var renderLayer : this.layers) renderLayer.render(poseStack, buffer, packedLight, player, l, k, partialTicks, ix, h, j);
        poseStack.popPose();
    }

    public @NotNull Vec3 getRenderOffset(AbstractClientPlayer entity, float partialTicks) {
        return entity.isCrouching() ? new Vec3(0.0, -0.125, 0.0) : super.getRenderOffset(entity, partialTicks);
    }

    protected void setupRotations(AbstractClientPlayer entityLiving, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        float f = entityLiving.getSwimAmount(partialTicks);
        if (entityLiving.isFallFlying()) {
            super.setupRotations(entityLiving, poseStack, ageInTicks, rotationYaw, partialTicks);
            float g = entityLiving.getFallFlyingTicks() + partialTicks;
            float h = Mth.clamp(g * g / 100.0F, 0.0F, 1.0F);
            if (!entityLiving.isAutoSpinAttack()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(h * (-90.0F - entityLiving.getXRot())));
            }

            Vec3 vec3 = entityLiving.getViewVector(partialTicks);
            Vec3 vec32 = entityLiving.getDeltaMovementLerped(partialTicks);
            double d = vec32.horizontalDistanceSqr();
            double e = vec3.horizontalDistanceSqr();
            if (d > 0.0 && e > 0.0) {
                double i = (vec32.x * vec3.x + vec32.z * vec3.z) / Math.sqrt(d * e);
                double j = vec32.x * vec3.z - vec32.z * vec3.x;
                poseStack.mulPose(Axis.YP.rotation((float)(Math.signum(j) * Math.acos(i))));
            }
        } else if (f > 0.0F) {
            super.setupRotations(entityLiving, poseStack, ageInTicks, rotationYaw, partialTicks);
            float gx = entityLiving.isInWater() ? -90.0F - entityLiving.getXRot() : -90.0F;
            float hx = Mth.lerp(f, 0.0F, gx);
            poseStack.mulPose(Axis.XP.rotationDegrees(hx));
            if (entityLiving.isVisuallySwimming()) {
                poseStack.translate(0.0F, -1.0F, 0.3F);
            }
        } else {
            super.setupRotations(entityLiving, poseStack, ageInTicks, rotationYaw, partialTicks);
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(AbstractClientPlayer entity) {
        return entity.getSkinTextureLocation();
    }
}
