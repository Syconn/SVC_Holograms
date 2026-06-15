package mod.syconn.svc.utils.generic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
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
        buffer.addVertex(matrix4f, (float) x, (float) y, (float) 0).setColor(r, g, b, a);
        buffer.addVertex(matrix4f, (float) x, (float) pMaxY, (float) 0).setColor(r, g, b, a);
        buffer.addVertex(matrix4f, (float) pMaxX, (float) pMaxY, (float) 0).setColor(r, g, b, a);
        buffer.addVertex(matrix4f, (float) pMaxX, (float) y, (float) 0).setColor(r, g, b, a);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void blitNineSliced(GuiGraphics graphics, ResourceLocation atlasLocation, int x, int y, int width, int height, int sliceSize, int uOffset, int vOffset, int textureWidth, int textureHeight) {
        blitNineSliced(graphics, atlasLocation, x, y, width, height, sliceSize, sliceSize, sliceSize, sliceSize, uOffset, vOffset, textureWidth, textureHeight);
    }

    private static void blitNineSliced(GuiGraphics graphics, ResourceLocation atlasLocation, int x, int y, int width, int height, int leftSliceWidth, int topSliceHeight, int rightSliceWidth, int bottomSliceHeight, int uWidth, int vHeight, int textureX, int textureY) {
        leftSliceWidth = Math.min(leftSliceWidth, width / 2);
        rightSliceWidth = Math.min(rightSliceWidth, width / 2);
        topSliceHeight = Math.min(topSliceHeight, height / 2);
        bottomSliceHeight = Math.min(bottomSliceHeight, height / 2);
        if (width == uWidth && height == vHeight) {
            graphics.blit(atlasLocation, x, y, textureX, textureY, width, height);
        } else if (height == vHeight) {
            graphics.blit(atlasLocation, x, y, textureX, textureY, leftSliceWidth, height);
            blitRepeating(graphics, atlasLocation, x + leftSliceWidth, y, width - rightSliceWidth - leftSliceWidth, height, textureX + leftSliceWidth, textureY, uWidth - rightSliceWidth - leftSliceWidth, vHeight);
            graphics.blit(atlasLocation, x + width - rightSliceWidth, y, textureX + uWidth - rightSliceWidth, textureY, rightSliceWidth, height);
        } else if (width == uWidth) {
            graphics.blit(atlasLocation, x, y, textureX, textureY, width, topSliceHeight);
            blitRepeating(graphics, atlasLocation, x, y + topSliceHeight, width, height - bottomSliceHeight - topSliceHeight, textureX, textureY + topSliceHeight, uWidth, vHeight - bottomSliceHeight - topSliceHeight);
            graphics.blit(atlasLocation, x, y + height - bottomSliceHeight, textureX, textureY + vHeight - bottomSliceHeight, width, bottomSliceHeight);
        } else {
            graphics.blit(atlasLocation, x, y, textureX, textureY, leftSliceWidth, topSliceHeight);
            blitRepeating(graphics, atlasLocation, x + leftSliceWidth, y, width - rightSliceWidth - leftSliceWidth, topSliceHeight, textureX + leftSliceWidth, textureY, uWidth - rightSliceWidth - leftSliceWidth, topSliceHeight);
            graphics.blit(atlasLocation, x + width - rightSliceWidth, y, textureX + uWidth - rightSliceWidth, textureY, rightSliceWidth, topSliceHeight);
            graphics.blit(atlasLocation, x, y + height - bottomSliceHeight, textureX, textureY + vHeight - bottomSliceHeight, leftSliceWidth, bottomSliceHeight);
            blitRepeating(graphics, atlasLocation, x + leftSliceWidth, y + height - bottomSliceHeight, width - rightSliceWidth - leftSliceWidth, bottomSliceHeight, textureX + leftSliceWidth, textureY + vHeight - bottomSliceHeight, uWidth - rightSliceWidth - leftSliceWidth, bottomSliceHeight);
            graphics.blit(atlasLocation, x + width - rightSliceWidth, y + height - bottomSliceHeight, textureX + uWidth - rightSliceWidth, textureY + vHeight - bottomSliceHeight, rightSliceWidth, bottomSliceHeight);
            blitRepeating(graphics, atlasLocation, x, y + topSliceHeight, leftSliceWidth, height - bottomSliceHeight - topSliceHeight, textureX, textureY + topSliceHeight, leftSliceWidth, vHeight - bottomSliceHeight - topSliceHeight);
            blitRepeating(graphics, atlasLocation, x + leftSliceWidth, y + topSliceHeight, width - rightSliceWidth - leftSliceWidth, height - bottomSliceHeight - topSliceHeight, textureX + leftSliceWidth, textureY + topSliceHeight, uWidth - rightSliceWidth - leftSliceWidth, vHeight - bottomSliceHeight - topSliceHeight);
            blitRepeating(graphics, atlasLocation, x + width - rightSliceWidth, y + topSliceHeight, leftSliceWidth, height - bottomSliceHeight - topSliceHeight, textureX + uWidth - rightSliceWidth, textureY + topSliceHeight, rightSliceWidth, vHeight - bottomSliceHeight - topSliceHeight);
        }
    }

    private static void blitRepeating(GuiGraphics graphics, ResourceLocation atlasLocation, int x, int y, int width, int height, int uOffset, int vOffset, int sourceWidth, int sourceHeight) {
        int i = x;

        int j;
        for(IntIterator intIterator = slices(width, sourceWidth); intIterator.hasNext(); i += j) {
            j = intIterator.nextInt();
            int k = (sourceWidth - j) / 2;
            int l = y;

            int m;
            for(IntIterator intIterator2 = slices(height, sourceHeight); intIterator2.hasNext(); l += m) {
                m = intIterator2.nextInt();
                int n = (sourceHeight - m) / 2;
                graphics.blit(atlasLocation, i, l, uOffset + k, vOffset + n, j, m);
            }
        }
    }

    private static IntIterator slices(int target, int total) {
        int i = Mth.positiveCeilDiv(target, total);
        return new Divisor(target, i);
    }
}
