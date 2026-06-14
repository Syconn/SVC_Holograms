package mod.syconn.svc.client.render.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.syconn.svc.network.packets.client.UpdateProjectorCachePacket;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.server.savedData.extra.CallData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class HoloProjectorDebugRenderer {

    public static List<CallData.BlockReceiver> PROJECTORS = new ArrayList<>();

    public static UpdateProjectorCachePacket playerJoinedServer(ServerPlayer player) {
        return new UpdateProjectorCachePacket(HologramNetwork.get(player.serverLevel()).getDebugData());
    }

    public static UpdateProjectorCachePacket playerLeftServer() {
        return new UpdateProjectorCachePacket(new ArrayList<>());
    }

    public static UpdateProjectorCachePacket playerChangedDimension(ServerPlayer player) {
        return new UpdateProjectorCachePacket(HologramNetwork.get(player.serverLevel()).getDebugData());
    }

    public static void renderBlockOutline(PoseStack poseStack, MultiBufferSource buffer, double camX, double camY, double camZ) {
        Matrix4f mat = poseStack.last().pose();

        for (var receiver : PROJECTORS) {
            if (receiver.callID == null) continue;

            VertexConsumer vc = buffer.getBuffer(RenderType.debugLineStrip(1.0f));
            Vec3 pos = new Vec3(receiver.pos.pos().getX() - camX, receiver.pos.pos().getY() - camY, receiver.pos.pos().getZ() - camZ);
            float x = (float) pos.x(), y = (float) pos.y(), z = (float) pos.z(), s = 1f;
            long mostSigBits = receiver.callID.getMostSignificantBits();
            int r = (int) ((mostSigBits >> 32) & 0xFF), g = (int) ((mostSigBits >> 16) & 0xFF), b = (int) (mostSigBits & 0xFF), a = 255;

            vc.addVertex(mat, x, y + s, z).setColor(r,g,b,a);
            vc.addVertex(mat, x + s, y + s, z).setColor(r,g,b,a);
            vc.addVertex(mat, x + s, y + s, z + s).setColor(r,g,b,a);
            vc.addVertex(mat, x, y + s, z + s).setColor(r,g,b,a);
            vc.addVertex(mat, x, y + s, z).setColor(r,g,b,a);

            vc.addVertex(mat, x, y, z).setColor(r,g,b,a);
            vc.addVertex(mat, x + s, y, z).setColor(r,g,b,a);
            vc.addVertex(mat, x + s, y, z + s).setColor(r,g,b,a);
            vc.addVertex(mat, x, y, z + s).setColor(r,g,b,a);
            vc.addVertex(mat, x, y, z).setColor(r,g,b,a);

            vc.addVertex(mat, x, y, z).setColor(r,g,b,a);
            vc.addVertex(mat, x, y + s, z).setColor(r,g,b,a);
            vc.addVertex(mat, x + s, y, z).setColor(r,g,b,a);
            vc.addVertex(mat, x + s, y + s, z).setColor(r,g,b,a);
            vc.addVertex(mat, x + s, y, z + s).setColor(r,g,b,a);
            vc.addVertex(mat, x + s, y + s, z + s).setColor(r,g,b,a);
            vc.addVertex(mat, x, y, z + s).setColor(r,g,b,a);
            vc.addVertex(mat, x, y + s, z + s).setColor(r,g,b,a);
        }
    }
}
