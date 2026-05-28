package mod.syconn.svc.utils.generic;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

    public static <T extends LivingEntity> void lerpLeftArmToDegrees(HumanoidModel<T> model, float delta, float pitch, float yaw, float roll) {
        model.leftArm.xRot = Mth.rotLerp(delta, model.leftArm.xRot * Mth.DEG_TO_RAD, pitch * Mth.DEG_TO_RAD) * Mth.RAD_TO_DEG;
        model.leftArm.yRot = Mth.rotLerp(delta, model.leftArm.yRot * Mth.DEG_TO_RAD, yaw * Mth.DEG_TO_RAD) * Mth.RAD_TO_DEG;
        model.leftArm.zRot = Mth.rotLerp(delta, model.leftArm.zRot * Mth.DEG_TO_RAD, roll * Mth.DEG_TO_RAD) * Mth.RAD_TO_DEG;
    }

    public static <T extends LivingEntity> void lerpRightArmToDegrees(HumanoidModel<T> model, float delta, float pitch, float yaw, float roll) {
        model.rightArm.xRot = Mth.rotLerp(delta, model.rightArm.xRot * Mth.DEG_TO_RAD, pitch * Mth.DEG_TO_RAD) * Mth.RAD_TO_DEG;
        model.rightArm.yRot = Mth.rotLerp(delta, model.rightArm.yRot * Mth.DEG_TO_RAD, yaw * Mth.DEG_TO_RAD) * Mth.RAD_TO_DEG;
        model.rightArm.zRot = Mth.rotLerp(delta, model.rightArm.zRot * Mth.DEG_TO_RAD, roll * Mth.DEG_TO_RAD) * Mth.RAD_TO_DEG;
    }

    public static AABB getBounds(Collection<Vector3f> vectors) {
        var min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        var max = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

        for (var v : vectors) {
            if (v.x < min.x) min.set(v.x, min.y, min.z);
            if (v.y < min.y) min.set(min.x, v.y, min.z);
            if (v.z < min.z) min.set(min.x, min.y, v.z);

            if (v.x > max.x) max.set(v.x, max.y, max.z);
            if (v.y > max.y) max.set(max.x, v.y, max.z);
            if (v.z > max.z) max.set(max.x, max.y, v.z);
        }

        return new AABB(new Vec3(min), new Vec3(max));
    }

    public static boolean isLeftHanded(ItemDisplayContext renderMode) {
        return renderMode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || renderMode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }
}
