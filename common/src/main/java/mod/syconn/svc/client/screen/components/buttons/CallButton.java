package mod.syconn.svc.client.screen.components.buttons;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.core.ModSounds;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.generic.MathUtil;
import mod.syconn.svc.utils.interfaces.IWidgetComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CallButton extends ExpandedButton implements IWidgetComponent {

    private static final ResourceLocation HOLOGRAM_SCREEN = Constants.withId("textures/gui/hologram_screen.png");
    private final float scalar;
    private final Type type;

    public CallButton(int xPos, int yPos, Type type, String hover, OnPress handler) {
        this(xPos, yPos, 1.0f, type, hover, handler);
    }

    public CallButton(int xPos, int yPos, float scalar, Type type, String hover, OnPress handler) {
        super(xPos, yPos, (int)(20 * scalar), (int)(20 * scalar), Component.literal(hover), handler);
        this.type = type;
        this.scalar = scalar;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushPose();

        var uX = type == Type.START ? 0 : 20;
        var uY = isHovered ? 58 : 38;
        guiGraphics.pose().scale(this.scalar, this.scalar, this.scalar);
        guiGraphics.blit(HOLOGRAM_SCREEN, (int) (this.getX() * (1 / this.scalar)), (int) (this.getY() * (1 / this.scalar)), uX, uY, 20, 20);

        guiGraphics.pose().popPose();

        if (isHovered) guiGraphics.renderTooltip(GameInstance.getClient().font, this.getMessage(), mouseX, mouseY);
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(MathUtil.randomChoice(ModSounds.HOLOGRAM_BUTTON1, ModSounds.HOLOGRAM_BUTTON2, ModSounds.HOLOGRAM_BUTTON3).get(), 1.0F));
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        START,
        END
    }
}
