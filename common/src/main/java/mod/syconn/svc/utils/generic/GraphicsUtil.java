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

    public static void renderHSVSquare(GuiGraphics graphics, int x, int y, int width, int height, Function<Integer, Integer> hsvColor) {
        for (int k = 0; k < width; k++) {
            var hsv = hsvColor.apply(k);
            var color = ColorUtil.hsvToRgbInt(ColorUtil.hsvGetH(hsv), ColorUtil.hsvGetS(hsv), ColorUtil.hsvGetV(hsv));
            fillRect(graphics, x + k, y, 1, height, FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color), 255);
        }
    }

    public static void fillRect(GuiGraphics graphics, int x, int y, int width, int height, int rgba) {
        fillRect(graphics, x, y, width, height, FastColor.ARGB32.red(rgba), FastColor.ARGB32.green(rgba), FastColor.ARGB32.blue(rgba), FastColor.ARGB32.alpha(rgba));
    }

    public static void fillRect(GuiGraphics graphics, int x, int y, int width, int height, int r, int g, int b, int a) {
        int pMaxX = x + width;
        int pMaxY = y + height;
        Matrix4f matrix4f = graphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(matrix4f, (float)x, (float)y, (float)0).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix4f, (float)x, (float)pMaxY, (float)0).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix4f, (float)pMaxX, (float)pMaxY, (float)0).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix4f, (float)pMaxX, (float)y, (float)0).color(r, g, b, a).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();
    }

    public static void blitWithBorder(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder) {
        int fillerWidth = textureWidth - leftBorder - rightBorder;
        int fillerHeight = textureHeight - topBorder - bottomBorder;
        int canvasWidth = width - leftBorder - rightBorder;
        int canvasHeight = height - topBorder - bottomBorder;
        int xPasses = canvasWidth / fillerWidth;
        int remainderWidth = canvasWidth % fillerWidth;
        int yPasses = canvasHeight / fillerHeight;
        int remainderHeight = canvasHeight % fillerHeight;

       graphics.blit(texture, x, y, u, v, leftBorder, topBorder);
       graphics.blit(texture, x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder);
       graphics.blit(texture, x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder);
       graphics.blit(texture, x + leftBorder + canvasWidth, y + topBorder + canvasHeight, u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder);

        for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++) {
           graphics.blit(texture, x + leftBorder + (i * fillerWidth), y, u + leftBorder, v, (i == xPasses ? remainderWidth : fillerWidth), topBorder);
           graphics.blit(texture, x + leftBorder + (i * fillerWidth), y + topBorder + canvasHeight, u + leftBorder, v + topBorder + fillerHeight, (i == xPasses ? remainderWidth : fillerWidth), bottomBorder);

            for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
               graphics.blit(texture, x + leftBorder + (i * fillerWidth), y + topBorder + (j * fillerHeight), u + leftBorder, v + topBorder, (i == xPasses ? remainderWidth : fillerWidth), (j == yPasses ? remainderHeight : fillerHeight));
        }

        for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
           graphics.blit(texture, x, y + topBorder + (j * fillerHeight), u, v + topBorder, leftBorder, (j == yPasses ? remainderHeight : fillerHeight));
           graphics.blit(texture, x + leftBorder + canvasWidth, y + topBorder + (j * fillerHeight), u + leftBorder + fillerWidth, v + topBorder, rightBorder, (j == yPasses ? remainderHeight : fillerHeight));
        }
    }
}
