package mod.syconn.svc.client.screen.components.buttons;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.interfaces.IWidgetComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SearchButton  extends ExpandedButton implements IWidgetComponent {

    private static final ResourceLocation HOLOGRAM_SCREEN = Constants.withId("textures/gui/hologram_screen.png");

    public SearchButton(int xPos, int yPos, String hover, OnPress handler) {
        super(xPos, yPos, 10, 10, Component.literal(hover), handler);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(HOLOGRAM_SCREEN, this.getX(), this.getY(), 104, 56, 10, 10);
        if (isHovered) guiGraphics.renderTooltip(GameInstance.getClient().font, this.getMessage(), mouseX, mouseY);
    }
}
