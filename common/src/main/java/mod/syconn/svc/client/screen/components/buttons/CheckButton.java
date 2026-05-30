package mod.syconn.svc.client.screen.components.buttons;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.interfaces.IWidgetComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CheckButton extends ExpandedButton implements IWidgetComponent {

    private static final ResourceLocation HOLOGRAM_SCREEN = Constants.withId("textures/gui/hologram_screen.png");
    private final Type type;

    public CheckButton(int xPos, int yPos, Type type, String hoverText, OnPress handler) {
        super(xPos, yPos, 10, 10, Component.literal(hoverText), handler);
        this.type = type;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(HOLOGRAM_SCREEN, this.getX(), this.getY(), 108 + (this.type == Type.CHECK ? 10 : 0), 38, 10, 10);
        if (isHovered) guiGraphics.renderTooltip(GameInstance.getClient().font, this.getMessage(), mouseX, mouseY);
    }

    public enum Type {
        CHECK,
        CROSS
    }
}
