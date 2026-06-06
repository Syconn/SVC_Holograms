package mod.syconn.svc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.blockentity.HoloProjectorBlockEntity;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.client.HologramData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HoloProjectorBlockEntityRenderer implements BlockEntityRenderer<HoloProjectorBlockEntity> {

    private static final ResourceLocation HOLOGRAM_TEXTURE = new ResourceLocation("svc", "textures/block/holo/hologram_ring.png");
    private static final ResourceLocation BEAM = new ResourceLocation("minecraft", "textures/entity/beacon_beam.png");
    private final HashMap<UUID, HologramData> SOLO_RENDERER = new HashMap<>();
    private final HashMap<UUID, Map<UUID, HologramData>> MULI_RENDERER = new HashMap<>();

    public HoloProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(HoloProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() != null) { //&& (!blockEntity.getSoloRender().isEmpty() || !blockEntity.getRenderables().isEmpty())
            float time = blockEntity.getLevel().getGameTime() + partialTick;
            renderRing(poseStack, buffer, time * -1.3f, 0.3f + noise(time, 1) * 0.06f, 0.2f, 1f + noise(time, 1) * 0.5f);
            renderRing(poseStack, buffer, time, 0.25f + noise(time, 2) * 0.05f, 0.15f, 0.8f);
            renderRing(poseStack, buffer, time * -0.7f, 0.2f + noise(time, 3) * 0.04f, 0.125f, 0.5f + noise(time, 1) * 0.4f);
            renderRing(poseStack, buffer, time * 0.7f, 0.15f + noise(time, 4) * 0.03f, 0.1f, 0.3f);
            renderRing(poseStack, buffer, time * 1.8f, 0.1f + noise(time, 5) * 0.02f, 0.085f, 0.1f + noise(time, 1) * 0.2f);
        }


        if (!blockEntity.getSoloRender().isEmpty()) {
            var soloRenderer = this.getSoloRenderer(blockEntity.getReceiverUUID(), blockEntity.getSoloRender());
            if (soloRenderer.activeRender()) {
                poseStack.pushPose();
                poseStack.translate(0.5f, 0.2f, 0.5f);
                poseStack.mulPose(Axis.YN.rotationDegrees((float) blockEntity.getRotation()));
                poseStack.translate(soloRenderer.getInterpolatedPosition().x, soloRenderer.getInterpolatedPosition().y, soloRenderer.getInterpolatedPosition().z);
                soloRenderer.getRenderer().render(poseStack, buffer, partialTick, LightTexture.FULL_BLOCK);
                poseStack.popPose();
            }
        } else {
            blockEntity.getRenderables().forEach((uuid, pos) -> this.createMultiplayerRenderer(blockEntity.getReceiverUUID(), uuid, pos));

            for (var entry : getMulitplayerMap(blockEntity.getReceiverUUID()).entrySet()) {
                var multiRender = this.getMultiplayerRenderer(blockEntity.getReceiverUUID(), entry.getKey());
                if (multiRender.activeRender()) {
                    poseStack.pushPose();
                    poseStack.translate(0.5f, 0.5f, 0.5f);
                    poseStack.translate(multiRender.getInterpolatedPosition().x, multiRender.getInterpolatedPosition().y, multiRender.getInterpolatedPosition().z);
                    multiRender.getRenderer().render(poseStack, buffer, partialTick, LightTexture.FULL_BLOCK);
                    poseStack.popPose();
                }

                if (!blockEntity.getRenderables().containsKey(multiRender.getPlayerID())) multiRender.endCall();
            }
        }
    }

    private HologramData getSoloRenderer(UUID receiverID, String playerName) {
        var data = SOLO_RENDERER.get(receiverID);
        if (data != null && data.getRenderName().equals(playerName)) return data;
        return SOLO_RENDERER.compute(receiverID, (_u, d) -> d == null ? new HologramData(playerName) : d.generateInformationByName(playerName));
    }

    private HologramData getMultiplayerRenderer(UUID receiverID, UUID playerID) {
        return MULI_RENDERER.computeIfAbsent(receiverID, u -> new HashMap<>()).get(playerID);
    }

    private void createMultiplayerRenderer(UUID receiverID, UUID playerID, Vec3 pos) {
        MULI_RENDERER.computeIfAbsent(receiverID, u -> new HashMap<>())
                .compute(playerID, (uuid, hologramData) -> hologramData == null ? new HologramData(uuid, pos) : hologramData.setActiveRender(true, pos));
    }

    private Map<UUID, HologramData> getMulitplayerMap(UUID receiverID) {
        return MULI_RENDERER.computeIfAbsent(receiverID, u -> new HashMap<>());
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public boolean shouldRenderOffScreen(HoloProjectorBlockEntity blockEntity) {
        if (GameInstance.getClient().getCameraEntity() == null) return false;
        return shouldRender(blockEntity, GameInstance.getClient().getCameraEntity().getEyePosition());
    }

    private void renderRing(PoseStack poseStack, MultiBufferSource bufferSource, float time, float radius, float height, float alpha) {
        poseStack.pushPose();
        poseStack.translate(0.5, height, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 8.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        var vc = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(HOLOGRAM_TEXTURE));
        var mat = poseStack.last().pose();
        int r = 0x55, g = 0xFF, b = 0xFF;
        vertex(vc, mat, -radius, -radius, 0, 0, r, g, b, alpha);
        vertex(vc, mat, radius, -radius, 1, 0, r, g, b, alpha);
        vertex(vc, mat, radius, radius, 1, 1, r, g, b, alpha);
        vertex(vc, mat, -radius, radius, 0, 1, r, g, b, alpha);
        poseStack.popPose();
    }

    private void vertex(VertexConsumer vc, Matrix4f mat, float x, float y, float u, float v, int r, int g, int b, float alpha) {
        vc.vertex(mat, x, y, (float) 0).color(r, g, b, (int)(alpha * 255)).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0, 1, 0).endVertex();
    }

    private float noise(float time, float seed) {
        var hash = (int)(time * 3.0f);
        hash ^= (int)(seed * 374761393);
        hash *= 1664525;
        hash += 1013904223;
        hash ^= (hash >> 13);
        hash *= 1274126177;
        hash ^= (hash >> 16);
        return (hash & 0xFFFF) / 65535f;
    }
}
