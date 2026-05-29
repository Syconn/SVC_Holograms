package mod.syconn.svc.client.render.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.syconn.svc.network.packets.client.UpdateProjectorCache;
import mod.syconn.svc.server.savedData.HologramNetwork;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HoloProjectorDebugRenderer {

    public static Map<UUID, BlockPos> PROJECTORS = new HashMap<>();

    public static UpdateProjectorCache playerJoinedServer(ServerPlayer player) {
        return new UpdateProjectorCache(HologramNetwork.get(player.serverLevel()).getDebugData());
    }

    public static UpdateProjectorCache playerLeftServer() {
        return new UpdateProjectorCache(new HashMap<>());
    }

    public static UpdateProjectorCache playerChangedDimension(ServerPlayer player) {
        return new UpdateProjectorCache(HologramNetwork.get(player.serverLevel()).getDebugData());
    }

    public static void renderBlockOutline(PoseStack poseStack, MultiBufferSource buffer, double camX, double camY, double camZ) {
        Matrix4f mat = poseStack.last().pose();

        for (var entry : PROJECTORS.entrySet()) {
            VertexConsumer vc = buffer.getBuffer(RenderType.debugLineStrip(1.0f));
            Vec3 pos = new Vec3(entry.getValue().getX() - camX, entry.getValue().getY() - camY, entry.getValue().getZ() - camZ);
            float x = (float) pos.x(), y = (float) pos.y(), z = (float) pos.z(), s = 1f;
            long mostSigBits = entry.getKey().getMostSignificantBits();
            int r = (int) ((mostSigBits >> 32) & 0xFF), g = (int) ((mostSigBits >> 16) & 0xFF), b = (int) (mostSigBits & 0xFF), a = 255;

            vc.vertex(mat, x, y + s, z).color(r,g,b,a).endVertex();
            vc.vertex(mat, x + s, y + s, z).color(r,g,b,a).endVertex();
            vc.vertex(mat, x + s, y + s, z + s).color(r,g,b,a).endVertex();
            vc.vertex(mat, x, y + s, z + s).color(r,g,b,a).endVertex();
            vc.vertex(mat, x, y + s, z).color(r,g,b,a).endVertex();

            vc.vertex(mat, x, y, z).color(r,g,b,a).endVertex();
            vc.vertex(mat, x + s, y, z).color(r,g,b,a).endVertex();
            vc.vertex(mat, x + s, y, z + s).color(r,g,b,a).endVertex();
            vc.vertex(mat, x, y, z + s).color(r,g,b,a).endVertex();
            vc.vertex(mat, x, y, z).color(r,g,b,a).endVertex();

            vc.vertex(mat, x, y, z).color(r,g,b,a).endVertex();
            vc.vertex(mat, x, y + s, z).color(r,g,b,a).endVertex();
            vc.vertex(mat, x + s, y, z).color(r,g,b,a).endVertex();
            vc.vertex(mat, x + s, y + s, z).color(r,g,b,a).endVertex();
            vc.vertex(mat, x + s, y, z + s).color(r,g,b,a).endVertex();
            vc.vertex(mat, x + s, y + s, z + s).color(r,g,b,a).endVertex();
            vc.vertex(mat, x, y, z + s).color(r,g,b,a).endVertex();
            vc.vertex(mat, x, y + s, z + s).color(r,g,b,a).endVertex();
        }
    }
}
