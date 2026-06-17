package mod.syconn.svc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.blockentity.HoloProjectorBlockEntity;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.client.HologramData;
import mod.syconn.svc.utils.client.ParticleEvent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class HoloProjectorBlockEntityRenderer implements BlockEntityRenderer<HoloProjectorBlockEntity> { // TODO SOMETHING WITH PLAYER RENDERING

    private static final ResourceLocation HOLOGRAM_TEXTURE = Constants.withId("textures/block/holo/hologram_ring.png");
    private final HashMap<UUID, HologramData> SOLO_RENDERER = new HashMap<>();
    private final HashMap<UUID, Map<UUID, HologramData>> MULI_RENDERER = new HashMap<>();

    public HoloProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void render(HoloProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        blockEntity.getRenderables().forEach((uuid, pos) -> this.createMultiplayerRenderer(blockEntity.getReceiverUUID(), uuid, pos));
        var renderSolo = true;

        if (blockEntity.getLevel() != null && blockEntity.isActive()) {
            float time = blockEntity.getLevel().getGameTime() + partialTick;
            renderRing(poseStack, buffer, time * -1.3f, 0.3f + noise(time, 1) * 0.06f, 0.2f, 1f + noise(time, 1) * 0.5f);
            renderRing(poseStack, buffer, time, 0.25f + noise(time, 2) * 0.05f, 0.15f, 0.8f);
            renderRing(poseStack, buffer, time * -0.7f, 0.2f + noise(time, 3) * 0.04f, 0.125f, 0.5f + noise(time, 1) * 0.4f);
            renderRing(poseStack, buffer, time * 0.7f, 0.15f + noise(time, 4) * 0.03f, 0.1f, 0.3f);
            renderRing(poseStack, buffer, time * 1.8f, 0.1f + noise(time, 5) * 0.02f, 0.085f, 0.1f + noise(time, 1) * 0.2f);
        }

        for (var entry : getMulitplayerMap(blockEntity.getReceiverUUID()).entrySet()) {
            var multiRender = this.getMultiplayerRenderer(blockEntity.getReceiverUUID(), entry.getKey());
            if (multiRender.activeRender()) {
                poseStack.pushPose();
                poseStack.translate(0.5f, 0.5f, 0.5f);
                poseStack.translate(multiRender.getInterpolatedPosition().x, multiRender.getInterpolatedPosition().y, multiRender.getInterpolatedPosition().z);
                multiRender.getRenderer().render(poseStack, buffer, partialTick, LightTexture.FULL_BLOCK);
                this.updateVFX(blockEntity, multiRender.getInterpolatedPosition(), multiRender.getAnimationScale(partialTick));
                poseStack.popPose();
                renderSolo = false;
            }

            if (!blockEntity.getRenderables().containsKey(multiRender.getProfile().getId())) multiRender.endCall();
        }

        if (renderSolo) {
            var soloRenderer = this.getSoloRenderer(blockEntity.getReceiverUUID(), blockEntity.getSoloRender());
            if (soloRenderer.activeRender()) {
                if (soloRenderer.isTextureLoaded()) {
                    poseStack.pushPose();
                    poseStack.translate(0.5f, 0.2f, 0.5f);
                    poseStack.mulPose(Axis.YN.rotationDegrees((float) blockEntity.getRotation()));
                    soloRenderer.getRenderer().render(poseStack, buffer, partialTick, LightTexture.FULL_BLOCK);
                    this.updateVFX(blockEntity, new Vec3(0, -0.5, 0), soloRenderer.getAnimationScale(partialTick));
                    poseStack.popPose();
                }
            } else SOLO_RENDERER.remove(blockEntity.getReceiverUUID());
        }
    }

    private HologramData getSoloRenderer(UUID receiverID, String playerName) {
        return SOLO_RENDERER.compute(receiverID, (_u, d) -> {
            if (d != null && ((Objects.equals(d.getProfile().getName(), playerName) && !playerName.isEmpty()))) return d;
            return d == null ? new HologramData(playerName, receiverID) : d.generateInformationByName(playerName, receiverID);
        });
    }

    private HologramData getMultiplayerRenderer(UUID receiverID, UUID playerID) {
        return MULI_RENDERER.computeIfAbsent(receiverID, u -> new HashMap<>()).get(playerID);
    }

    private void createMultiplayerRenderer(UUID receiverID, UUID playerID, Vec3 pos) {
        MULI_RENDERER.computeIfAbsent(receiverID, u -> new HashMap<>()).compute(playerID, (uuid, hologramData) -> hologramData == null ? new HologramData(uuid, receiverID, pos) : hologramData.setActiveRender(true, pos));
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

    private void updateVFX(HoloProjectorBlockEntity blockEntity, Vec3 vec3, float scale) {
        if (scale <= 0.01f) return;
        if (blockEntity.getLevel() == null) return;

        final var level = blockEntity.getLevel();
        final var worldPos = blockEntity.getBlockPos();
        final var pos = vec3.add(worldPos.getX(), worldPos.getY() + 0.5f, worldPos.getZ());
        final float time = level.getGameTime();
        final double cx = pos.x + 0.5, cz = pos.z + 0.5;
        final float radius = 0.7f * scale, speed = 0.02f + scale * 0.12f, pull = 0.02f + scale * 0.15f;
        final int points = Math.max(1, (int) (2 + scale * 4));

        for (int i = 0; i < points; i++) {
            float angle = (float) (Math.random() * Math.PI * 2.0), rot = time * speed, a = angle + rot;
            var chargeStrength = 1.0f - scale;
            if (chargeStrength > 0.05f) {
                var chargeParticles = Math.max(1, (int) (chargeStrength * 3));

                for (int j = 0; j < chargeParticles; j++) {
                    var chargeAngle = a + (float) (Math.random() * 0.5 - 0.25);
                    var chargeRadius = radius * (0.9f + (float) Math.random() * 0.4f);
                    double x = cx + Math.cos(chargeAngle) * chargeRadius, z = cz + Math.sin(chargeAngle) * chargeRadius;
                    var particlePos = new Vec3(x, pos.y + 0.05, z);
                    var velocity = new Vec3((cx - x) * (pull * 1.8f), 0.03 + chargeStrength * 0.05, (cz - z) * (pull * 1.8f));
                    blockEntity.addParticleEvent(new ParticleEvent(particlePos, velocity, ParticleTypes.END_ROD));
                }
            }

            if (scale > 0.15f && scale < 0.95f) {
                double x = cx + Math.cos(a) * radius, z = cz + Math.sin(a) * radius;
                var particlePos = new Vec3(x, pos.y + 0.03, z);
                var velocity = new Vec3((cx - x) * pull, 0.015 + scale * 0.02, (cz - z) * pull);
                blockEntity.addParticleEvent(new ParticleEvent(particlePos, velocity, new DustParticleOptions(new Vector3f(0.65f, 0.90f, 1.00f), 0.9f)));
            }

            if (scale > 0.15f) {
                var innerRadius = radius * 0.55f;
                double x = cx + Math.cos(a) * innerRadius, z = cz + Math.sin(a) * innerRadius;
                var particlePos = new Vec3(x, pos.y + 0.01, z);
                var velocity = new Vec3((cx - x) * pull * 1.4f, 0.01 + scale * 0.025, (cz - z) * pull * 1.4f);
                blockEntity.addParticleEvent(new ParticleEvent(particlePos, velocity, new DustParticleOptions(new Vector3f(0.20f, 0.85f, 1.00f), 1.15f)));
            }
        }
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
        vc.addVertex(mat, x, y, (float) 0).setColor(r, g, b, (int) (alpha * 255)).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0, 1, 0);
    }

    private float noise(float time, float seed) {
        var hash = (int) (time * 3.0f);
        hash ^= (int) (seed * 374761393);
        hash *= 1664525;
        hash += 1013904223;
        hash ^= (hash >> 13);
        hash *= 1274126177;
        hash ^= (hash >> 16);
        return (hash & 0xFFFF) / 65535f;
    }
}
