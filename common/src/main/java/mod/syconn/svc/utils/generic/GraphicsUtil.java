package mod.syconn.svc.utils.generic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;

import java.util.function.Function;

public class GraphicsUtil {

    public static void blitSliced(GuiGraphics graphics, ResourceLocation location, int x, int y, int height, int uWidth, int uHeight, int uX, int uY) {
        graphics.blit(location, x, y, uX, uY, uWidth, uHeight);
        for (int i = 0; i < height; i++) graphics.blit(location, x, y + uHeight + i, uX, uY + 1, uWidth, 1);
        graphics.blit(location, x, y + uHeight + height, uX, uY + 2, uWidth, uHeight);
    }

    public static void fillRect(GuiGraphics graphics, int x, int y, int width, int height, int rgba) {
        fillRect(graphics, x, y, width, height, FastColor.ARGB32.red(rgba), FastColor.ARGB32.green(rgba), FastColor.ARGB32.blue(rgba), FastColor.ARGB32.alpha(rgba));
    }

    public static void fillRect(GuiGraphics graphics, int x, int y, int width, int height, int r, int g, int b, int a) {
        int pMaxX = x + width;
        int pMaxY = y + height;
        var matrix4f = graphics.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrix4f, (float)x, (float)y, (float)0).setColor(r, g, b, a);
        buffer.addVertex(matrix4f, (float)x, (float)pMaxY, (float)0).setColor(r, g, b, a);
        buffer.addVertex(matrix4f, (float)pMaxX, (float)pMaxY, (float)0).setColor(r, g, b, a);
        buffer.addVertex(matrix4f, (float)pMaxX, (float)y, (float)0).setColor(r, g, b, a);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }
}
