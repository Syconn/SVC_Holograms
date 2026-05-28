package mod.syconn.svc.client.screen.components.buttons;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.core.ModSounds;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.generic.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class RefreshButton extends ExpandedButton {

    private static final ResourceLocation COMPONENTS = Constants.withId("textures/gui/components.png");

    public RefreshButton(int xPos, int yPos, OnPress handler) {
        super(xPos, yPos, 10, 10, Component.empty(), b -> handler.onPress());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(COMPONENTS, this.getX(), this.getY(), 20, 176, this.width, this.height);

        if (isHovered) guiGraphics.renderTooltip(GameInstance.getClient().font, createNarrationMessage(), mouseX, mouseY);
    }

    @Override
    protected @NotNull MutableComponent createNarrationMessage() {
        return Component.literal("Refresh");
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(MathUtil.randomChoice(ModSounds.HOLOGRAM_BUTTON1, ModSounds.HOLOGRAM_BUTTON2, ModSounds.HOLOGRAM_BUTTON3).get(), 1.0F));
    }

    @Environment(EnvType.CLIENT)
    public interface OnPress {
        void onPress();
    }
}
