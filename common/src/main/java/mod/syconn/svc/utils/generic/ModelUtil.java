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
        int[] v = quad.getVertices();
        Matrix4f mat = pose.pose();
        Matrix3f normalMat = pose.normal();

        Vec3i n = quad.getDirection().getNormal();

        float nx = n.getX();
        float ny = n.getY();
        float nz = n.getZ();

        for (int i = 0; i < 4; i++) {

            int o = i * 8;

            float x = Float.intBitsToFloat(v[o]);
            float y = Float.intBitsToFloat(v[o + 1]);
            float z = Float.intBitsToFloat(v[o + 2]);

            float u = Float.intBitsToFloat(v[o + 4]);
            float vv = Float.intBitsToFloat(v[o + 5]);

            consumer.vertex(mat, x, y, z).color((int)(red * 255),
                            (int)(green * 255), (int)(blue * 255),
                            (int)(alpha * 255)
                    )
                    .uv(u, vv)
                    .overlayCoords(overlay)
                    .uv2(light)
                    .normal(normalMat, nx, ny, nz)
                    .endVertex();
        }
    }

    public static boolean isLeftHanded(ItemDisplayContext renderMode) {
        return renderMode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || renderMode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }
}
