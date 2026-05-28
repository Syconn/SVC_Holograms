package mod.syconn.svc.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public class Constants {

    public static final String MOD = "svc";
    public static final RandomSource RANDOM = RandomSource.createNewThreadLocalInstance();

    public static ResourceLocation withId(String s) {
        return new ResourceLocation(MOD, s);
    }
}
