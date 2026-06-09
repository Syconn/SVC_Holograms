package mod.syconn.svc.client.screen.components.buttons;

import mod.syconn.svc.core.ModSounds;
import mod.syconn.svc.utils.generic.FontUtil;
import mod.syconn.svc.utils.generic.GraphicsUtil;
import mod.syconn.svc.utils.generic.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class ExpandedButton extends Button {

    public ExpandedButton(int xPos, int yPos, int width, int height, Component displayString, OnPress handler) {
        this(xPos, yPos, width, height, displayString, handler, DEFAULT_NARRATION);
    }

    public ExpandedButton(int xPos, int yPos, int width, int height, Component displayString, OnPress handler, CreateNarration createNarration) {
        super(xPos, yPos, width, height, displayString, handler, createNarration);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        int k = !this.active ? 0 : (this.isHovered ? 2 : 1);
        GraphicsUtil.blitWithBorder(guiGraphics, WIDGETS_LOCATION, this.getX(), this.getY(), 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2);

        final FormattedText buttonText = FontUtil.ellipsis(mc.font, this.getMessage(), this.width - 6);
        guiGraphics.drawCenteredString(mc.font, Language.getInstance().getVisualOrder(buttonText), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, getFGColor());
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(MathUtil.randomChoice(ModSounds.HOLOGRAM_BUTTON1, ModSounds.HOLOGRAM_BUTTON2, ModSounds.HOLOGRAM_BUTTON3).get(), 1.0F));
    }

    protected int getFGColor() {
        return this.active ? 16777215 : 10526880;
    }
}
