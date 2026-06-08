package mod.syconn.svc.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.syconn.svc.client.model.HologramModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {

    @Final @Shadow
    private TextureAtlas armorTrimAtlas;

    @Shadow
    protected abstract void renderGlint(PoseStack poseStack, MultiBufferSource buffer, int packedLight, A model);

    @Shadow
    protected abstract void setPartVisibility(A model, EquipmentSlot slot);

    @Shadow
    protected abstract ResourceLocation getArmorLocation(ArmorItem armorItem, boolean layer2, @Nullable String suffix);

    public HumanoidArmorLayerMixin(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Inject(at = @At("HEAD"), method = "renderArmorPiece", cancellable = true)
    public void renderArmorPiece(PoseStack poseStack, MultiBufferSource buffer, T livingEntity, EquipmentSlot slot, int packedLight, A model, CallbackInfo ci) {
        var itemStack = livingEntity.getItemBySlot(slot);
        if (!(getParentModel() instanceof HologramModel)) return;
        if (!(itemStack.getItem() instanceof ArmorItem armorItem)) return;
        if (armorItem.getEquipmentSlot() != slot) return;

        this.getParentModel().copyPropertiesTo(model);
        this.setPartVisibility(model, slot);

        var leggings = slot == EquipmentSlot.LEGS;
        var time = livingEntity.tickCount + Minecraft.getInstance().getFrameTime();
        float flicker = 0.80f + 0.20f * Mth.sin(time * 0.25f);
        float alpha = (160.0f * flicker) / 255.0f;
        float holoR = 0.05f, holoG = 0.30f, holoB = 0.60f;

        if (armorItem instanceof DyeableArmorItem dyeableArmorItem) {
            var i = dyeableArmorItem.getColor(itemStack);
            float tintR = ((i >> 16) & 0xFF) / 255.0F, tintG = ((i >> 8) & 0xFF) / 255.0F, tintB = (i & 0xFF) / 255.0F;
            float r = tintR * holoR, g = tintG * holoG, b = tintB * holoB;
            svc$renderModel(poseStack, buffer, packedLight, armorItem, model, leggings, r, g, b, alpha, null);
            svc$renderModel(poseStack, buffer, packedLight, armorItem, model, leggings, 1.0f, 1.0f, 1.0f, alpha, "overlay");
        } else svc$renderModel(poseStack, buffer, packedLight, armorItem, model, leggings, holoR, holoG, holoB, alpha, null);

        ArmorTrim.getTrim(livingEntity.level().registryAccess(), itemStack).ifPresent(trim -> this.svc$renderTrim(armorItem.getMaterial(), poseStack, buffer, packedLight, trim, model, leggings, time));
        if (itemStack.hasFoil()) this.renderGlint(poseStack, buffer, packedLight, model);
        ci.cancel();
    }

    @Unique
    private void svc$renderTrim(ArmorMaterial armorMaterial, PoseStack poseStack, MultiBufferSource buffer, int packedLight, ArmorTrim trim, A model, boolean innerTexture, float time) {
        var sprite = this.armorTrimAtlas.getSprite(innerTexture ? trim.innerTexture(armorMaterial) : trim.outerTexture(armorMaterial));
        var consumer = sprite.wrap(buffer.getBuffer(Sheets.armorTrimsSheet()));
        var flicker = 0.80f + 0.20f * Mth.sin(time * 0.25f);
        var alpha = (160.0f * flicker) / 255.0f;
        float r = 0.25f, g = 0.75f, b = 1.00f;
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, alpha);
    }

    @Unique
    private void svc$renderModel(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ArmorItem armorItem, A model, boolean withGlint, float red, float green, float blue, float alpha, @Nullable String armorSuffix) {
        var texture = this.getArmorLocation(armorItem, withGlint, armorSuffix);
        var consumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
    }
}
