package mod.syconn.svc.utils.generic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.syconn.svc.utils.Constants;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MathUtil {

    public static final Vec3 V3D_POS_X = new Vec3(1, 0, 0);
    public static final Vec3 V3D_NEG_X = new Vec3(-1, 0, 0);
    public static final Vec3 V3D_POS_Y = new Vec3(0, 1, 0);
    public static final Vec3 V3D_NEG_Y = new Vec3(0, -1, 0);
    public static final Vec3 V3D_POS_Z = new Vec3(0, 0, 1);
    public static final Vec3 V3D_NEG_Z = new Vec3(0, 0, -1);

    public static final Vector3f V3F_POS_X = new Vector3f(1, 0, 0);
    public static final Vector3f V3F_NEG_X = new Vector3f(-1, 0, 0);
    public static final Vector3f V3F_POS_Y = new Vector3f(0, 1, 0);
    public static final Vector3f V3F_NEG_Y = new Vector3f(0, -1, 0);
    public static final Vector3f V3F_POS_Z = new Vector3f(0, 0, 1);
    public static final Vector3f V3F_NEG_Z = new Vector3f(0, 0, -1);

    public static final Quaternionf ROT_X_POS45 = new Quaternionf().rotationX((float)Math.toRadians(45));

    public static final Quaternionf ROT_Y_POS10 = new Quaternionf().rotationY((float)Math.toRadians(10));
    public static final Quaternionf ROT_Y_POS90 = new Quaternionf().rotationY((float)Math.toRadians(90));
    public static final Quaternionf ROT_Y_180 = new Quaternionf().rotationY((float)Math.toRadians(180));

    public static final Quaternionf ROT_Z_POS80 = new Quaternionf().rotationZ((float)Math.toRadians(80));

    public static final Quaternionf IDENTITY = new Quaternionf();

    private static final Vec3 UP = new Vec3(0, 1, 0);
    private static final Vec3 FORWARD = new Vec3(0, 0, 1);

    public static final int TICKS_PER_SECOND = 20;
    public static final float SPEED_OF_SOUND = 275f / TICKS_PER_SECOND;

    public static Vec3i floorInt(Vec3 v) {
        return new Vec3i(Mth.floor(v.x), Mth.floor(v.y), Mth.floor(v.z));
    }

    public static float remap(float x, float iMin, float iMax, float oMin, float oMax) {
        return (x - iMin) / (iMax - iMin) * (oMax - oMin) + oMin;
    }

    public static Vec3 anglesToLook(float pitch, float yaw) {
        var x = -Mth.sin(yaw * Mth.RAD_TO_DEG) * Mth.cos(pitch * Mth.RAD_TO_DEG);
        var y = -Mth.sin(pitch * Mth.RAD_TO_DEG);
        var z = Mth.cos(yaw * Mth.RAD_TO_DEG) * Mth.cos(pitch * Mth.RAD_TO_DEG);

        return new Vec3(x, y, z).normalize();
    }

    public static Vec3 lookToAngles(Vec3 forward) {
        forward = forward.normalize();

        var yaw = -(float)Math.atan2(forward.x, forward.z);
        var pitch = -(float)Math.asin(forward.y);

        return new Vec3(pitch * Mth.DEG_TO_RAD, yaw * Mth.DEG_TO_RAD, 0);
    }

    public static float toRadians(float degrees) {
        return degrees * Mth.RAD_TO_DEG;
    }

    public static void translateRotation(PoseStack poseStack, Direction direction, float x, float y, float z) {
        switch (direction) {
            case NORTH -> poseStack.translate(-x, y, -z);
            case SOUTH -> poseStack.translate(x, y, z);
            case EAST -> poseStack.translate(z, y, -x);
            case WEST -> poseStack.translate(-z, y, x);
        }
    }

    public static void translateRotation(PoseStack poseStack, Direction direction, Vec3 vec3) {
        translateRotation(poseStack, direction, (float) vec3.x, (float) vec3.y, (float) vec3.z);
    }

    public static Quaternionf getEastRotation(Direction direction) {
        return switch (direction) {
            case DOWN -> new Quaternionf().rotationXYZ(0, 0, (float)(Math.PI / -2));
            case UP -> new Quaternionf().rotationXYZ(0, 0, (float)(Math.PI / 2));
            case NORTH -> new Quaternionf().rotationXYZ(0, (float)(Math.PI / 2), 0);
            case SOUTH -> new Quaternionf().rotationXYZ(0, (float)(Math.PI / -2), 0);
            case WEST -> new Quaternionf().rotationXYZ(0, (float)Math.PI, 0);
            case EAST -> new Quaternionf().rotationXYZ(0, 0, 0);
        };
    }

    public static Quaternionf getNorthRotation(Direction direction) {
        return switch (direction) {
            case NORTH -> Axis.YP.rotationDegrees(0);
            case EAST -> Axis.YP.rotationDegrees(270);
            case SOUTH -> Axis.YP.rotationDegrees(180);
            case WEST -> Axis.YP.rotationDegrees(90);
            case UP -> Axis.XP.rotationDegrees(90);
            case DOWN -> Axis.XP.rotationDegrees(270);
        };
    }

    public static Vec3 reflect(Vec3 incident, Vec3 normal) {
        var reflection = normal.scale(2 * normal.dot(incident)).subtract(incident);
        return reflection.scale(-1);
    }

    public static int wrap(int value, int max) {
        var range = max + 1;
        value = ((value % range) + range) % range;
        return value;
    }

    @SafeVarargs
    public static <R> R randomChoice(R... choices) {
        return choices[Constants.RANDOM.nextIntBetweenInclusive(0, choices.length - 1)];
    }

    public static Vec3 toEulerAngles(Quaternionf q) {
        var forward = rotate(V3D_NEG_Z, q);
        return lookToAngles(forward);
    }

    public static Quaternionf lookAt(Vec3 sourcePoint, Vec3 destPoint) {
        var forwardVector = destPoint.subtract(sourcePoint).normalize();

        var dot = FORWARD.dot(forwardVector);

        if (Math.abs(dot - (-1.0f)) < 0.000001f)
            return new Quaternionf().rotationAxis(Mth.PI, new Vector3f((float)UP.x, (float)UP.y, (float)UP.z));
        if (Math.abs(dot - (1.0f)) < 0.000001f)
            return new Quaternionf(IDENTITY);

        var rotAngle = Math.acos(dot);
        var rotAxis = FORWARD.cross(forwardVector);
        rotAxis = rotAxis.normalize();

        return new Quaternionf().rotationAxis((float)rotAngle, rotAxis.toVector3f());
    }

    public static Vec3 rotate(Vec3 self, Quaternionf q) {
        var u = new Vec3(q.x, q.y, q.z);
        var s = q.w;
        return u.scale(2.0f * u.dot(self)).add(self.scale(s * s - u.dot(u))).add(u.cross(self).scale(2.0f * s));
    }

    public static void rotateTowards(Quaternionf self, Vec3 orientation, float speed) {
        self.normalize();
        var vec2 = rotate(orientation, self);
        var cross = orientation.cross(vec2).scale(-1.0);
        var axis = cross.normalize();
        var f1 = (float)cross.length();
        var other = new Quaternionf().rotationAxis(speed * f1, axis.toVector3f());
        other.mul(self);
        self.set(other);
    }

    public static Quaternionf getRotationTowards(Vec3 from, Vec3 to) {
        var cross = from.cross(to);
        var w = (float)(Math.sqrt(from.lengthSqr() * to.lengthSqr()) + from.dot(to));
        var q = new Quaternionf(w, (float)cross.x, (float)cross.y, (float)cross.z);
        q.normalize();
        return q;
    }

    public static float calculateDopplerShift(Entity a, Entity b) { // TODO: move doppler handling to OpenAL through SoundSystem's updateListenerPosition call?
        var velA = a.position().subtract(a.xOld, a.yOld, a.zOld);
        var velB = b.position().subtract(b.xOld, b.yOld, b.zOld);

        var posA = a.getEyePosition();
        var posB = b.getEyePosition();

        var relativeSpeed = posA.distanceTo(posB) - posA.add(velA).distanceTo(posB.add(velB));

        return Mth.clamp((float)(relativeSpeed / SPEED_OF_SOUND), -1, 1);
    }

    /**
     * Finds a global vector in local terms
     */
    public static Vec3 project(Vec3 v, Quaternionf q) {
        var c = new Quaternionf(q);
        c.conjugate();
        return rotate(v, c);
    }

    public static Quaternionf slerp(Quaternionf start, Quaternionf end, float t) {
        // Only unit quaternions are valid rotations.
        // Normalize to avoid undefined behavior.
        start.normalize();
        end.normalize();

        // Compute the cosine of the angle between the two vectors.
        double dot = start.dot(end);

        // If the dot product is negative, slerp won't take
        // the shorter path. Note that end and -end are equivalent when
        // the negation is applied to all four components. Fix by
        // reversing one quaternion.
        if (dot < 0.0f) {
            end.scale(-1);
            dot = -dot;
        }

        if (dot > 0.9995) {
            // If the inputs are too close for comfort, linearly interpolate
            // and normalize the result.

            var f = 1 - t;
            var a = f * start.w + t * end.w;
            var b = f * start.x + t * end.x;
            var c = f * start.y + t * end.y;
            var d = f * start.z + t * end.z;

            var result = new Quaternionf(b, c, d, a);
            result.normalize();
            return result;
        }

        // Since dot is in range [0, DOT_THRESHOLD], acos is safe
        var theta_0 = Math.acos(dot);        // theta_0 = angle between input vectors
        var theta = theta_0 * t;          // theta = angle between start and result
        var sin_theta = Math.sin(theta);     // compute this value only once
        var sin_theta_0 = Math.sin(theta_0); // compute this value only once

        var f1 = Math.cos(theta) - dot * sin_theta / sin_theta_0;  // == sin(theta_0 - theta) / sin(theta_0)
        var f2 = sin_theta / sin_theta_0;

        var a = (float)(f1 * start.w + f2 * end.w);
        var b = (float)(f1 * start.x + f2 * end.x);
        var c = (float)(f1 * start.y + f2 * end.y);
        var d = (float)(f1 * start.z + f2 * end.z);

        var result = new Quaternionf(b, c, d, a);
        result.normalize();
        return result;
    }
}
