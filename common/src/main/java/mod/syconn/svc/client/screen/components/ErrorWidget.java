package mod.syconn.svc.client.screen.components;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ErrorWidget extends AbstractWidget {

    private String errorMsg = "";
    private int displayTicks = 0;

    public ErrorWidget(int x, int y) {
        super(x, y, 1, 1, Component.empty());
    }

    public void displayError(String errorMsg, int displayTicks) {
        this.errorMsg = errorMsg;
        this.displayTicks = displayTicks;
    }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (displayTicks > 0) {
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.literal(this.errorMsg).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), this.getX(), this.getY(), -1);
            this.displayTicks--;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.errorMsg);
    }
}
