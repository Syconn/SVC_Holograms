package mod.syconn.svc.utils.generic;

import mod.syconn.svc.utils.Constants;
import net.minecraft.util.Mth;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MathUtil {

    public static UUID combineUUID(UUID a, UUID b) {
        var combined = a.toString() + "|" + b.toString();
        return UUID.nameUUIDFromBytes(combined.getBytes(StandardCharsets.UTF_8));
    }

    public static float toRadians(float degrees) {
        return degrees * Mth.RAD_TO_DEG;
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
}
