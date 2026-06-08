package mod.syconn.svc.client.screen.components.buttons;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.interfaces.IWidgetComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LockButton extends ExpandedButton implements IWidgetComponent {

    private static final ResourceLocation HOLOGRAM_SCREEN = Constants.withId("textures/gui/hologram_screen.png");
    private Type type;

    public LockButton(int xPos, int yPos, Type type) {
        super(xPos, yPos, 10, 12, Component.literal(""), null);
        this.type = type;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(HOLOGRAM_SCREEN, this.getX(), this.getY(), 116 + (this.type == Type.LOCK ? 0 : 11), 56, 10, 12);
        if (isHovered) guiGraphics.renderTooltip(GameInstance.getClient().font, Component.literal((this.type == Type.LOCK ? "Private" : "Public") + " Call"), mouseX, mouseY);
    }

    @Override
    public void onPress() {
        this.type = this.type == Type.LOCK ? Type.UNLOCK : Type.LOCK;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        LOCK,
        UNLOCK
    }
}
