package mod.syconn.svc.client.screen.components.buttons;

import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.interfaces.IWidgetComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ToggleButton extends ExpandedButton implements IWidgetComponent {

    private static final ResourceLocation WIDGETS = Constants.withId("textures/gui/hologram_screen.png");
    private final Color color;
    private boolean active;
    private boolean locked;

    public ToggleButton(int x, int y, boolean active, Color color, OnPress handler) {
        super(x, y, 22, 12, Component.empty(), handler);
        this.active = active;
        this.color = color;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!locked) {
            super.onClick(mouseX, mouseY);
            this.active = !active;
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var u = isHovered ? 40 : 62;
        var v = !this.active ? 38 : this.color == Color.GREEN ? 50 : 62;
        guiGraphics.blit(WIDGETS, getX(), getY(), u, v, this.width, this.height);
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean active() {
        return this.active;
    }

    public enum Color {
        GREEN,
        YELLOW
    }

    @Environment(EnvType.CLIENT)
    public interface OnPress extends Button.OnPress {

        void onPress(ToggleButton button);

        @Override
        default void onPress(Button button) {
            this.onPress((ToggleButton) button);
        }
    }
}
