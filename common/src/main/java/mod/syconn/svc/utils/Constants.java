package mod.syconn.svc.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public class Constants {
    // https://linkie.shedaniel.dev/ for mapping conversions

    public static final String MOD = "svc";
    public static final RandomSource RANDOM = RandomSource.createNewThreadLocalInstance();

    public static ResourceLocation withId(String s) {
        return new ResourceLocation(MOD, s);
    }
}
