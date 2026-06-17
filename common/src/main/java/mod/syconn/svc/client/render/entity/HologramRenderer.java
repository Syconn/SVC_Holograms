package mod.syconn.svc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.client.model.HologramModel;
import mod.syconn.svc.utils.client.HologramData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
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
            this.setModelProperties(player);
            this.render(player, player.getYRot(), partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    @Override
    protected @Nullable RenderType getRenderType(AbstractClientPlayer livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(this.data.getSkin() == null ? DefaultPlayerSkin.getDefaultTexture() : this.data.getSkin());
    }

    @Override
    protected void renderNameTag(AbstractClientPlayer entity, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) { }

    private void setModelProperties(AbstractClientPlayer clientPlayer) {
        PlayerModel<AbstractClientPlayer> playerModel = this.getModel();
        if (this.data.isStaticRender()) {
            HologramModel hologramModel = (HologramModel) this.getModel();
            hologramModel.setAllVisible(true);
            hologramModel.rightPants.visible = true;
            hologramModel.leftPants.visible = true;
            hologramModel.leftSleeve.visible = true;
            hologramModel.rightSleeve.visible = true;
            hologramModel.jacket.visible = true;
            hologramModel.hat.visible = true;
            clientPlayer.setYHeadRot(0);
        } else {
            playerModel.setAllVisible(true);
            playerModel.hat.visible = clientPlayer.isModelPartShown(PlayerModelPart.HAT);
            playerModel.jacket.visible = clientPlayer.isModelPartShown(PlayerModelPart.JACKET);
            playerModel.leftPants.visible = clientPlayer.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
            playerModel.rightPants.visible = clientPlayer.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
            playerModel.leftSleeve.visible = clientPlayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
            playerModel.rightSleeve.visible = clientPlayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
            playerModel.crouching = clientPlayer.isCrouching();
            HumanoidModel.ArmPose armPose = getArmPose(clientPlayer, InteractionHand.MAIN_HAND);
            HumanoidModel.ArmPose armPose2 = getArmPose(clientPlayer, InteractionHand.OFF_HAND);
            if (armPose.isTwoHanded()) armPose2 = clientPlayer.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;

            if (clientPlayer.getMainArm() == HumanoidArm.RIGHT) {
                playerModel.rightArmPose = armPose;
                playerModel.leftArmPose = armPose2;
            } else {
                playerModel.rightArmPose = armPose2;
                playerModel.leftArmPose = armPose;
            }
        }
    }

    private static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.isEmpty()) return HumanoidModel.ArmPose.EMPTY;
        else {
            if (player.getUsedItemHand() == hand && player.getUseItemRemainingTicks() > 0) {
                UseAnim useAnim = itemStack.getUseAnimation();
                if (useAnim == UseAnim.BLOCK) return HumanoidModel.ArmPose.BLOCK;
                else if (useAnim == UseAnim.BOW) return HumanoidModel.ArmPose.BOW_AND_ARROW;
                else if (useAnim == UseAnim.SPEAR) return HumanoidModel.ArmPose.THROW_SPEAR;
                else if (useAnim == UseAnim.CROSSBOW && hand == player.getUsedItemHand()) return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                else if (useAnim == UseAnim.SPYGLASS) return HumanoidModel.ArmPose.SPYGLASS;
                else if (useAnim == UseAnim.TOOT_HORN) return HumanoidModel.ArmPose.TOOT_HORN;
                else if (useAnim == UseAnim.BRUSH) return HumanoidModel.ArmPose.BRUSH;
            } else if (!player.swinging && itemStack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemStack)) return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            return HumanoidModel.ArmPose.ITEM;
        }
    }

    public void render(AbstractClientPlayer entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        this.model.attackTime = this.getAttackAnim(entity, partialTicks);
        this.model.riding = entity.isPassenger();
        this.model.young = entity.isBaby();
        var f = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        var g = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        var h = g - f;
        if (entity.isPassenger() && entity.getVehicle() instanceof LivingEntity livingEntity) {
            f = Mth.rotLerp(partialTicks, livingEntity.yBodyRotO, livingEntity.yBodyRot);
            h = g - f;
            var i = Mth.wrapDegrees(h);
            if (i < -85.0F) i = -85.0F;
            if (i >= 85.0F) i = 85.0F;
            f = g - i;
            if (i * i > 2500.0F) f += i * 0.2F;
            h = g - f;
        }

        var j = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        if (isEntityUpsideDown(entity)) {
            j *= -1.0F;
            h *= -1.0F;
        }

        h = Mth.wrapDegrees(h);
        if (entity.hasPose(Pose.SLEEPING)) {
            var direction = entity.getBedOrientation();
            if (direction != null) {
                float k = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                poseStack.translate(-direction.getStepX() * k, 0.0F, -direction.getStepZ() * k);
            }
        }

        var ix = entity.getScale();
        poseStack.scale(ix, ix, ix);
        var k = this.getBob(entity, partialTicks);
        setupRotations(entity, poseStack, k, f, partialTicks, ix);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        scale(entity, poseStack, partialTicks);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        var l = 0.0F;
        var m = 0.0F;
        if (!entity.isPassenger() && entity.isAlive()) {
            l = entity.walkAnimation.speed(partialTicks);
            m = entity.walkAnimation.position(partialTicks);
            if (entity.isBaby()) m *= 3.0F;
            if (l > 1.0F) l = 1.0F;
        }

        this.model.prepareMobModel(entity, m, l, partialTicks);
        this.model.setupAnim(entity, m, l, k, h, j);
        this.model.setAllVisible(true);
        var minecraft = Minecraft.getInstance();
        var bl = this.isBodyVisible(entity);
        var bl2 = !bl && !entity.isInvisibleTo(minecraft.player);
        var bl3 = minecraft.shouldEntityAppearGlowing(entity);
        var renderType = this.getRenderType(entity, bl, bl2, bl3);
        if (renderType != null) {
            VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
            int n = getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks));
            this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, n, bl2 ? 654311423 : -1);
        }

        if (!entity.isSpectator()) for (var renderLayer : this.layers) renderLayer.render(poseStack, buffer, packedLight, entity, m, l, partialTicks, k, h, j);
        poseStack.popPose();
    }

    public @NotNull Vec3 getRenderOffset(AbstractClientPlayer entity, float partialTicks) {
        return entity.isCrouching() ? new Vec3(0.0, -0.125, 0.0) : super.getRenderOffset(entity, partialTicks);
    }

    protected void setupRotations(AbstractClientPlayer entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        var f = entity.getSwimAmount(partialTick);
        var g = entity.getViewXRot(partialTick);
        if (entity.isFallFlying()) {
            super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
            var h = (float)entity.getFallFlyingTicks() + partialTick;
            var i = Mth.clamp(h * h / 100.0F, 0.0F, 1.0F);
            if (!entity.isAutoSpinAttack()) poseStack.mulPose(Axis.XP.rotationDegrees(i * (-90.0F - g)));

            var vec3 = entity.getViewVector(partialTick);
            var vec32 = entity.getDeltaMovementLerped(partialTick);
            var d = vec32.horizontalDistanceSqr();
            var e = vec3.horizontalDistanceSqr();
            if (d > (double)0.0F && e > (double)0.0F) {
                double j = (vec32.x * vec3.x + vec32.z * vec3.z) / Math.sqrt(d * e);
                double k = vec32.x * vec3.z - vec32.z * vec3.x;
                poseStack.mulPose(Axis.YP.rotation((float)(Math.signum(k) * Math.acos(j))));
            }
        } else if (f > 0.0F) {
            super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
            var h = entity.isInWater() ? -90.0F - g : -90.0F;
            var i = Mth.lerp(f, 0.0F, h);
            poseStack.mulPose(Axis.XP.rotationDegrees(i));
            if (entity.isVisuallySwimming()) poseStack.translate(0.0F, -1.0F, 0.3F);
        } else super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(AbstractClientPlayer entity) {
        return this.data.getSkin();
    }
}
