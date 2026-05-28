package mod.syconn.svc.client.screen.components;

import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.generic.GraphicsUtil;
import mod.syconn.svc.utils.interfaces.IWidgetComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.Function;

public class ScrollerWidget extends AbstractWidget implements IWidgetComponent {

    private static final ResourceLocation HOLOGRAM_SCREEN = Constants.withId("textures/gui/hologram_screen.png");
    private final Function<ScrollerWidget, Boolean> canScroll;
    private final Consumer<Integer> scrollTo;
    private int size;
    private float scrollOffs;

    public ScrollerWidget(int x, int y, int height, int size, Function<ScrollerWidget, Boolean> canScroll, Consumer<Integer> scrollTo) {
        super(x, y, 14, height + 2, Component.empty());
        this.canScroll = canScroll;
        this.scrollTo = scrollTo;
        this.size = Math.max(size, 0);
    }

    private int getTextureY() {
        return this.isHovered ? 84 : 96;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var scroll = size == 0 ? 0 : (height - 13) * this.scrollOffs;
        GraphicsUtil.blitSliced(guiGraphics, HOLOGRAM_SCREEN, this.getX(), this.getY(), height + 2, 14, 1, 84, 53);
        guiGraphics.blit(HOLOGRAM_SCREEN, this.getX() + 1, (int) (this.getY() + 1 + scroll), this.getTextureY(), 38, 12, 15);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!this.canScroll.apply(this)) return false;
        else {
            this.scrollOffs = this.subtractInputFromScroll(this.scrollOffs, delta);
            this.scrollTo.accept(getRowIndexForScroll(this.scrollOffs));
            return true;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.canScroll.apply(this)) {
            int i = getY() + 18;
            int j = i + 112;
            this.scrollOffs = ((float)mouseY - i - 7.5F) / (j - i - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.scrollTo.accept(getRowIndexForScroll(this.scrollOffs));
            return true;
        } else return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private int getRowIndexForScroll(float scrollOffs) {
        return Math.max((int)(scrollOffs * this.size + 0.5), 0);
    }

    private float subtractInputFromScroll(float scrollOffs, double input) {
        return Mth.clamp(scrollOffs - (float)(input / this.size), 0.0F, 1.0F);
    }

    public float setScroll(int rowIndex) {
        return Mth.clamp((float)rowIndex / this.size, 0.0F, 1.0F);
    }

    public void updateSize(int size) {
        this.size = Math.max(size, 0);
    }
}
