package mod.syconn.svc.utils.generic;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class ColorUtil {

    public static int tint(int src, int tint, TintMode tintMode) {
        var srcA = FastColor.ABGR32.alpha(src);
        var srcR = FastColor.ABGR32.red(src);
        var srcG = FastColor.ABGR32.green(src);
        var srcB = FastColor.ABGR32.blue(src);

        var tintA = FastColor.ABGR32.alpha(tint);
        if (tintA == 0) tintA = 255;
        var tintR = FastColor.ABGR32.red(tint);
        var tintG = FastColor.ABGR32.green(tint);
        var tintB = FastColor.ABGR32.blue(tint);

        switch (tintMode) {
            case Multiply -> {
                srcA = (srcA * tintA) / 255;
                srcR = (srcR * tintR) / 255;
                srcG = (srcG * tintG) / 255;
                srcB = (srcB * tintB) / 255;
            }
            case Add -> {
                srcR = Mth.clamp(srcR + tintR, 0, 255);
                srcG = Mth.clamp(srcG + tintG, 0, 255);
                srcB = Mth.clamp(srcB + tintB, 0, 255);
            }
            case Overlay -> {
                var srcRf = srcR / 255f;
                var srcGf = srcG / 255f;
                var srcBf = srcB / 255f;

                var tintRf = tintR / 255f;
                var tintGf = tintG / 255f;
                var tintBf = tintB / 255f;

                srcR = (int)(255 * Mth.clamp(srcRf < 0.5 ? 2 * srcRf * tintRf : (1 - 2 * (1 - srcRf) * (1 - tintRf)), 0, 1));
                srcG = (int)(255 * Mth.clamp(srcGf < 0.5 ? 2 * srcGf * tintGf : (1 - 2 * (1 - srcGf) * (1 - tintGf)), 0, 1));
                srcB = (int)(255 * Mth.clamp(srcBf < 0.5 ? 2 * srcBf * tintBf : (1 - 2 * (1 - srcBf) * (1 - tintBf)), 0, 1));
            }
        }

        return FastColor.ABGR32.color(srcA, srcB, srcG, srcR);
    }

    public static int grayScaled(int argb) {
        int alpha = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        int gray = (r + g + b) / 3;
        return FastColor.ARGB32.color(alpha, gray, gray, gray);
    }

    public static int hologramColor(int argb) {
        if (FastColor.ARGB32.alpha(argb) == 0) return argb;
        return tint(grayScaled(argb), FastColor.ARGB32.color(255, 255, 148, 0), TintMode.Overlay);
    }

    public static int packRgb(int r, int g, int b) {
        var rgb = (r & 0xFF);
        rgb = (rgb << 8) + (g & 0xFF);
        rgb = (rgb << 8) + (b & 0xFF);
        return rgb | 0xFF000000;
    }

    public static int packArgb(int r, int g, int b, int a) {
        var rgb = (a & 0xFF);
        rgb = (rgb << 8) + (r & 0xFF);
        rgb = (rgb << 8) + (g & 0xFF);
        rgb = (rgb << 8) + (b & 0xFF);
        return rgb;
    }

    public static int packFloatRgb(final float r, final float g, final float b) {
        return packFloatArgb(r, g, b, 0);
    }

    public static int packFloatArgb(final float r, final float g, final float b, final float a) {
        return packArgb((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255));
    }

    public static int packHsv(final float h, final float s, final float v) {
        return packArgb((int)(h * 255), (int)(s * 255), (int)(v * 255), 0);
    }

    public static int hsvToRgbInt(final float hue, final float saturation, final float value) {
        final var normalizedHue = (hue - (float)Math.floor(hue));
        final var h = (int)(normalizedHue * 6);
        final var f = normalizedHue * 6 - h;
        final var p = value * (1 - saturation);
        final var q = value * (1 - f * saturation);
        final var t = value * (1 - (1 - f) * saturation);

        return switch (h) {
            case 0 -> packFloatRgb(value, t, p);
            case 1 -> packFloatRgb(q, value, p);
            case 2 -> packFloatRgb(p, value, t);
            case 3 -> packFloatRgb(p, q, value);
            case 4 -> packFloatRgb(t, p, value);
            case 5 -> packFloatRgb(value, p, q);
            default -> 0;
        };
    }

    public static Vector3f hsvToRgb(final float hue, final float saturation, final float value) {
        final var normalizedHue = (hue - (float)Math.floor(hue));
        final var h = (int)(normalizedHue * 6);
        final var f = normalizedHue * 6 - h;
        final var p = value * (1 - saturation);
        final var q = value * (1 - f * saturation);
        final var t = value * (1 - (1 - f) * saturation);

        return switch (h) {
            case 0 -> new Vector3f(value, t, p);
            case 1 -> new Vector3f(q, value, p);
            case 2 -> new Vector3f(p, value, t);
            case 3 -> new Vector3f(p, q, value);
            case 4 -> new Vector3f(t, p, value);
            case 5 -> new Vector3f(value, p, q);
            default -> new Vector3f(0, 0, 0);
        };
    }

    public static int argbToAbgr(int value) {
        return FastColor.ABGR32.color((value >>> 24) & 0xFF, (value) & 0xFF, (value >>> 8) & 0xFF, (value >>> 16) & 0xFF);
    }

    public static float argbGetAf(int color) {
        return ((color & 0xFF000000) >> 24) / 255f;
    }

    public static float argbGetRf(int color) {
        return ((color & 0xFF0000) >> 16) / 255f;
    }

    public static float argbGetGf(int color) {
        return ((color & 0xFF00) >> 8) / 255f;
    }

    public static float argbGetBf(int color) {
        return (color & 0xFF) / 255f;
    }

    public static float hsvGetH(int color) {
        return ((color & 0xFF0000) >> 16) / 255f;
    }

    public static float hsvGetS(int color) {
        return ((color & 0xFF00) >> 8) / 255f;
    }

    public static float hsvGetV(int color) {
        return (color & 0xFF) / 255f;
    }

    public static float getAlpha(double layer) {
        return (float) Mth.clamp(1 - (layer / 100 + 0.4) / (1 + Math.exp(-0.3 * (layer - 22))), 0, 1);
    }

    public static float getSaturation(double layer, double target) {
        var layeredSat = Mth.clamp((layer / 400 + 0.76) / (1 + Math.exp(-0.27 * (layer - 10))), 0, 1);
        return (float)(layeredSat * target);
    }

    public static float getValue(double layer, double target) {
        return (float) Mth.lerp(AnimationUtil.outCubic((float)(layer / 75)), 1, target);
    }

    public static float getHue(double h, double x) {
        return (float) Mth.clamp(-0.06 * Math.exp(-0.011 * Math.pow(x - 6, 2)) + h, 0, 1);
    }

    public static String toResourceId(int color) {
        return String.format("%06x", color & 0xFFFFFF);
    }
    
    public enum TintMode {
        Add,
        Multiply,
        Overlay
    }
}
