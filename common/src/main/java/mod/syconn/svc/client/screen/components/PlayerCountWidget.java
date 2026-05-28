package mod.syconn.svc.client.screen.components;

import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.interfaces.IWidgetComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class PlayerCountWidget extends AbstractWidget implements IWidgetComponent {

    private static final ResourceLocation HOLOGRAM_SCREEN = Constants.withId("textures/gui/hologram_screen.png");
    private List<Component> players;

    public PlayerCountWidget(int x, int y) {
        super(x, y, 10, 13, Component.empty());
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var uX = !isHovered ? 84 : 94;
        guiGraphics.blit(HOLOGRAM_SCREEN, this.getX(), this.getY(), uX, 56, 10, 13);

        if (this.players != null) {
            var font = Minecraft.getInstance().font;
            var string = String.valueOf(players.size());
            guiGraphics.drawString(font, string, this.getX() - font.width(string) - 2, this.getY() + 3, -1);

            if (isHovered) guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, players, mouseX, mouseY);
        }
    }

    public void setPlayers(List<Component> players) {
        this.players = players;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
