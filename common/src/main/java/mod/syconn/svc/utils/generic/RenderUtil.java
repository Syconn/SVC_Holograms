package mod.syconn.svc.utils.generic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.MatrixUtil;
import mod.syconn.svc.mixin.client.ItemRendererAccessor;
import mod.syconn.svc.utils.client.HologramBufferSource;
import mod.syconn.svc.utils.interfaces.IItemExtensions;
import mod.syconn.svc.utils.interfaces.IModifiedItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class RenderUtil {

    private static int slotMainHand = 0;
    private static final ModelResourceLocation TRIDENT_MODEL = ModelResourceLocation.vanilla("trident", "inventory");
    private static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.vanilla("spyglass", "inventory");

    public static boolean handleReequipAnimation(@NotNull ItemStack from, @NotNull ItemStack to, int slot) {
        if (from.getItem() instanceof IItemExtensions ext) {
            boolean fromInvalid = from.isEmpty();
            boolean toInvalid = to.isEmpty();

            if (fromInvalid && toInvalid) return false;
            if (fromInvalid || toInvalid) return true;

            boolean changed = false;
            if (slot != -1) {
                changed = slot != slotMainHand;
                slotMainHand = slot;
            }
            return ext.shouldCauseReequipAnimation(from, to, changed);
        }
        return true;
    }

    public static void renderStaticHolographicItem(PoseStack poseStack, LivingEntity entity, HumanoidModel<? extends LivingEntity> model, HumanoidArm arm, ItemStack stack, ItemDisplayContext context, BakedModel bakedModel, MultiBufferSource buffer, int packedLight, float time) {
        final IModifiedItemRenderer itemRenderer = IModifiedItemRenderer.INSTANCES.get(stack.getItem().getClass());
        poseStack.pushPose();
        model.translateToHand(arm, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate((arm == HumanoidArm.LEFT ? -1 : 1) / 16.0F, 0.125F, -0.625F);
        itemRenderer.render(entity, model, stack, context, arm == HumanoidArm.LEFT, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, bakedModel);
        renderHolographicItem(poseStack, stack, context, bakedModel, arm == HumanoidArm.LEFT, buffer, packedLight, time);
        poseStack.popPose();
    }

    private static void renderHolographicItem(PoseStack poseStack, ItemStack itemStack, ItemDisplayContext displayContext, BakedModel model, boolean leftHand, MultiBufferSource buffer, int packedLight, float time) {
        if (!itemStack.isEmpty()) {
            poseStack.pushPose();
            final var bl = displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND || displayContext == ItemDisplayContext.FIXED;
            if (bl) {
                if (itemStack.is(Items.TRIDENT)) model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getModelManager().getModel(TRIDENT_MODEL);
                else if (itemStack.is(Items.SPYGLASS)) model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getModelManager().getModel(SPYGLASS_MODEL);
            }
            model.getTransforms().getTransform(displayContext).apply(leftHand, poseStack);
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            if (!model.isCustomRenderer() && (!itemStack.is(Items.TRIDENT) || bl)) {
                if (hasAnimatedTexture(itemStack) && itemStack.hasFoil()) {
                    poseStack.pushPose();
                    var pose = poseStack.last();
                    if (displayContext == ItemDisplayContext.GUI) MatrixUtil.mulComponentWise(pose.pose(), 0.5F);
                    else if (displayContext.firstPerson()) MatrixUtil.mulComponentWise(pose.pose(), 0.75F);
                    poseStack.popPose();
                }
                renderHolographicItemEffect(poseStack, itemStack, model, buffer, packedLight, time);
            } else {
                var hologramBuffer = new HologramBufferSource(buffer);
                ((ItemRendererAccessor) Minecraft.getInstance().getItemRenderer()).getBlockEntityRenderer().renderByItem(itemStack, displayContext, poseStack, hologramBuffer, packedLight, OverlayTexture.NO_OVERLAY);
            }
            poseStack.popPose();
        }
    }

    private static boolean hasAnimatedTexture(ItemStack stack) {
        return stack.is(ItemTags.COMPASSES) || stack.is(Items.CLOCK);
    }

    private static void renderHolographicItemEffect(PoseStack poseStack, ItemStack stack, BakedModel bakedModel, MultiBufferSource buffer, int packedLight, float time) {
        var consumer = buffer.getBuffer(RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS));
        var pose = poseStack.last();
        var random = RandomSource.create();
        float r = 0.05f, g = 0.2f, b = 0.4f, a = 0.50f;
        var itemColors = ((ItemRendererAccessor) Minecraft.getInstance().getItemRenderer()).getItemColors();

        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            for (BakedQuad quad : bakedModel.getQuads(null, direction, random)) {
                float red = r, green = g, blue = b;
                if (quad.isTinted()) {
                    int tint = itemColors.getColor(stack, quad.getTintIndex());
                    float tintR = ((tint >> 16) & 255) / 255.0f, tintG = ((tint >> 8) & 255) / 255.0f, tintB = (tint & 255) / 255.0f;
                    red *= tintR;
                    green *= tintG;
                    blue *= tintB;
                }
                ModelUtil.renderHoloQuadAlpha(consumer, pose, quad, red, green, blue, a, packedLight, OverlayTexture.NO_OVERLAY, time);
            }
        }

        random.setSeed(42L);
        for (BakedQuad quad : bakedModel.getQuads(null, null, random)) {
            float red = r, green = g, blue = b;
            if (quad.isTinted()) {
                int tint = itemColors.getColor(stack, quad.getTintIndex());
                float tintR = ((tint >> 16) & 255) / 255.0f, tintG = ((tint >> 8) & 255) / 255.0f, tintB = (tint & 255) / 255.0f;
                red *= tintR;
                green *= tintG;
                blue *= tintB;
            }
            ModelUtil.renderHoloQuadAlpha(consumer, pose, quad, red, green, blue, a, packedLight, OverlayTexture.NO_OVERLAY, time);
        }
    }
}
