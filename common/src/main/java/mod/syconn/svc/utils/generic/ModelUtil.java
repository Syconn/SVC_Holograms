package mod.syconn.svc.utils.generic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.Optional;

public class ModelUtil {

    public static Optional<ModelPart> getChild(ModelPart root, String name) {
        if (root.hasChild(name)) return Optional.of(root.getChild(name));
        return Optional.empty();
    }

    public static <T extends LivingEntity> void smartLerpArmsRadians(T entity, InteractionHand mainHand, HumanoidModel<?> model, float delta, float leftPitch, float leftYaw, float leftRoll, float rightPitch, float rightYaw, float rightRoll) {
        ModelPart leftArm = model.leftArm;
        ModelPart rightArm = model.rightArm;

        if ((entity.getMainArm() == HumanoidArm.LEFT) ^ (mainHand == InteractionHand.OFF_HAND)) {
            rightYaw = -rightYaw;
            rightRoll = -rightRoll;
            rightArm = model.leftArm;

            leftYaw = -leftYaw;
            leftRoll = -leftRoll;
            leftArm = model.rightArm;
        }

        rightArm.xRot = Mth.lerp(delta, rightArm.xRot, rightPitch);
        rightArm.yRot = Mth.lerp(delta, rightArm.yRot, rightYaw);
        rightArm.zRot = Mth.lerp(delta, rightArm.zRot, rightRoll);

        leftArm.xRot = Mth.lerp(delta, leftArm.xRot, leftPitch);
        leftArm.yRot = Mth.lerp(delta, leftArm.yRot, leftYaw);
        leftArm.zRot = Mth.lerp(delta, leftArm.zRot, leftRoll);
    }

    public static void renderQuadAlpha(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int light, int overlay) {
        var vertices = quad.getVertices();
        var matrix = pose.pose();
        var normalMatrix = pose.normal();
        var normal = quad.getDirection().getNormal();
        float nx = normal.getX(), ny = normal.getY(), nz = normal.getZ();

        for (int vertex = 0; vertex < 4; vertex++) {
            var offset = vertex * 8;
            float x = Float.intBitsToFloat(vertices[offset]), y = Float.intBitsToFloat(vertices[offset + 1]), z = Float.intBitsToFloat(vertices[offset + 2]);
            float u = Float.intBitsToFloat(vertices[offset + 4]), v = Float.intBitsToFloat(vertices[offset + 5]);
            consumer.vertex(matrix, x, y, z).color((int)(red * 255.0f), (int)(green * 255.0f), (int)(blue * 255.0f), (int)(alpha * 255.0f)).uv(u, v).overlayCoords(overlay).uv2(light).normal(normalMatrix, nx, ny, nz).endVertex();
        }
    }

    public static boolean isLeftHanded(ItemDisplayContext renderMode) {
        return renderMode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || renderMode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }
}
