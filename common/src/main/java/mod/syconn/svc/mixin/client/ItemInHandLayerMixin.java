package mod.syconn.svc.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.syconn.svc.utils.interfaces.IModifiedItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> extends ItemInHandLayer<T, M> {

    public ItemInHandLayerMixin(RenderLayerParent<T, M> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer, itemInHandRenderer);
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "HEAD"), cancellable = true)
    private void itemRendered(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (!itemStack.isEmpty()) {
            final BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(itemStack, livingEntity.level(), livingEntity, livingEntity.getId() + displayContext.ordinal());
            final IModifiedItemRenderer itemRenderer = IModifiedItemRenderer.INSTANCES.get(itemStack.getItem().getClass());
            if (itemRenderer != null) itemRenderer.render(livingEntity, (HumanoidModel<? extends LivingEntity>) getParentModel(), itemStack, displayContext, arm == HumanoidArm.LEFT, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, bakedModel);
            Minecraft.getInstance().getItemRenderer().render(itemStack, displayContext, arm == HumanoidArm.LEFT, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, bakedModel);
            ci.cancel();
        }
    }
}
