package mod.syconn.svc.client.render.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mod.syconn.svc.network.packets.client.UpdateProjectorCache;
import mod.syconn.svc.server.savedData.HologramNetwork;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HoloProjectorDebugRenderer {

    public static Map<UUID, BlockPos> PROJECTORS = new HashMap<>();
    public static boolean requestedRefresh = false;
    private static VertexBuffer vertexBuffer;

    @Environment(EnvType.SERVER)
    public static UpdateProjectorCache playerJoinedServer(ServerPlayer player) {
        return new UpdateProjectorCache(HologramNetwork.get(player.serverLevel()).getDebugData());
    }

    @Environment(EnvType.SERVER)
    public static UpdateProjectorCache playerLeftServer() {
        return new UpdateProjectorCache(new HashMap<>());
    }

    @Environment(EnvType.SERVER)
    public static UpdateProjectorCache playerChangedDimension(ServerPlayer player) {
        return new UpdateProjectorCache(HologramNetwork.get(player.serverLevel()).getDebugData());
    }

    public static void renderBlockOutline(PoseStack poseStack, Matrix4f projectionMatrix) {
        if (vertexBuffer == null || requestedRefresh) {
            requestedRefresh = false;
            vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

            var opacity = 1F;
            PROJECTORS.forEach((uuid, pos) -> {
                int color = uuidToRGBA(uuid);
                final float size = 1.0f;
                final int x = pos.getX(), y = pos.getY(), z = pos.getZ();

                final float red = (color >> 16 & 0xff) / 255f;
                final float green = (color >> 8 & 0xff) / 255f;
                final float blue = (color & 0xff) / 255f;

                buffer.vertex(x, y + size, z).color(red, green, blue, opacity);
                buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity);
                buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity);
                buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity);
                buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity);
                buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity);
                buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity);
                buffer.vertex(x, y + size, z).color(red, green, blue, opacity);

                // BOTTOM
                buffer.vertex(x + size, y, z).color(red, green, blue, opacity);
                buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity);
                buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity);
                buffer.vertex(x, y, z + size).color(red, green, blue, opacity);
                buffer.vertex(x, y, z + size).color(red, green, blue, opacity);
                buffer.vertex(x, y, z).color(red, green, blue, opacity);
                buffer.vertex(x, y, z).color(red, green, blue, opacity);
                buffer.vertex(x + size, y, z).color(red, green, blue, opacity);

                // Edge 1
                buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity);
                buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity);

                // Edge 2
                buffer.vertex(x + size, y, z).color(red, green, blue, opacity);
                buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity);

                // Edge 3
                buffer.vertex(x, y, z + size).color(red, green, blue, opacity);
                buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity);

                // Edge 4
                buffer.vertex(x, y, z).color(red, green, blue, opacity);
                buffer.vertex(x, y + size, z).color(red, green, blue, opacity);
            });

            BufferBuilder.RenderedBuffer build = buffer.end();
            vertexBuffer.bind();
            vertexBuffer.upload(build);
            VertexBuffer.unbind();
        }

        if (vertexBuffer != null) {
            Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();


            poseStack.pushPose();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            poseStack.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
            vertexBuffer.bind();
            vertexBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
            VertexBuffer.unbind();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    public static int uuidToRGBA(UUID uuid) {
        long mostSigBits = uuid.getMostSignificantBits();
        int r = (int) ((mostSigBits >> 32) & 0xFF);
        int g = (int) ((mostSigBits >> 16) & 0xFF);
        int b = (int) (mostSigBits & 0xFF);
        return FastColor.ARGB32.color(256, r, g, b);
    }
}
