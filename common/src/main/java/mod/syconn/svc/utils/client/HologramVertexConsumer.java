package mod.syconn.svc.utils.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

public class HologramVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;

    public HologramVertexConsumer(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull VertexConsumer vertex(double x, double y, double z) {
        return delegate.vertex(x, y, z);
    }

    @Override
    public @NotNull VertexConsumer color(int r, int g, int b, int a) {
        float tintR = 0.05f, tintG = 0.2f, tintB = 0.4f;
        r = (int)(r * tintR);
        g = (int)(g * tintG);
        b = (int)(b * tintB);
        a = Math.min(a, 127);
        return delegate.color(r, g, b, a);
    }

    @Override
    public @NotNull VertexConsumer uv(float u, float v) {
        return delegate.uv(u, v);
    }

    @Override
    public @NotNull VertexConsumer overlayCoords(int u, int v) {
        return delegate.overlayCoords(u, v);
    }

    @Override
    public @NotNull VertexConsumer uv2(int u, int v) {
        return delegate.uv2(u, v);
    }

    @Override
    public @NotNull VertexConsumer normal(float x, float y, float z) {
        return delegate.normal(x, y, z);
    }

    @Override
    public void endVertex() {
        delegate.endVertex();
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        delegate.defaultColor(r, g, b, a);
    }

    @Override
    public void unsetDefaultColor() {
        delegate.unsetDefaultColor();
    }
}
