package mod.syconn.svc.utils.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HologramBufferSource implements MultiBufferSource {

    private final MultiBufferSource parent;

    public HologramBufferSource(MultiBufferSource parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull VertexConsumer getBuffer(RenderType renderType) {
        return new HologramVertexConsumer(parent.getBuffer(convertRenderType(renderType)));
    }

    private RenderType convertRenderType(RenderType renderType) {
        var matcher = Pattern.compile("Optional\\[(.*?)\\]").matcher(renderType.toString());
        if (matcher.find()) return RenderType.entityTranslucentCull(ResourceLocation.parse(matcher.group(1)));
        else return renderType;
    }
}
