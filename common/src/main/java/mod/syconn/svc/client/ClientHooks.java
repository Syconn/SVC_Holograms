package mod.syconn.svc.client;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.client.screen.HologramScreen;
import mod.syconn.svc.client.sounds.HoloProjectorSoundInstance;
import mod.syconn.svc.utils.block.WorldPos;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ClientHooks {

    public static Screen createHologramScreen(WorldPos worldPos, @Nullable ItemStack stack) {
        return new HologramScreen(worldPos, stack);
    }

    public static void playerHoloSound(BlockPos pos) {
        GameInstance.getClient().getSoundManager().play(new HoloProjectorSoundInstance(pos));
    }
}
