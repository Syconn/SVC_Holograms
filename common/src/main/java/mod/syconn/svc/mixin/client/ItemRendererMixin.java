package mod.syconn.svc.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.syconn.svc.utils.generic.ColorUtil;
import mod.syconn.svc.utils.interfaces.IModifiedItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    public abstract BakedModel getModel(ItemStack stack, Level level, LivingEntity entity, int seed);

    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", at = @At("HEAD"), cancellable = true)
    public void renderItem(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, Level level, int combinedLight, int combinedOverlay, int seed, CallbackInfo ci) {
        if (!itemStack.isEmpty()) {
            final BakedModel bakedModel = this.getModel(itemStack, level, entity, seed);
            final IModifiedItemRenderer itemRenderer = IModifiedItemRenderer.INSTANCES.get(itemStack.getItem().getClass());
            if (itemRenderer != null && itemRenderer.render(entity, null, itemStack, displayContext, leftHand, poseStack, buffer, combinedLight, combinedOverlay, bakedModel)) ci.cancel();
        }
    }
}
