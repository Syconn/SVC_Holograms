package mod.syconn.svc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.utils.client.HologramData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
            poseStack.pushPose();

            var scale = this.data.getAnimationScale(partialTicks);
            poseStack.scale(scale, scale, scale);
            this.render(this.data.getPlayer(), 0, partialTicks, poseStack, buffer, packedLight);

            poseStack.popPose();
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(AbstractClientPlayer entity) {
        return this.data.getSkin();
    }

    @Override
    protected @Nullable RenderType getRenderType(AbstractClientPlayer livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return this.data.isItem() ? RenderType.itemEntityTranslucentCull(this.getTextureLocation(livingEntity)) : RenderType.entityTranslucentCull(this.getTextureLocation(livingEntity));
    }

    @Override
    protected void renderNameTag(AbstractClientPlayer entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {}
}
