package mod.syconn.svc.client;

import mod.syconn.svc.client.screen.HologramScreen;
import mod.syconn.svc.client.sounds.BlockHoloProjectorSoundInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ClientHooks {

    public static Screen createHologramScreen(BlockPos pos, @Nullable ItemStack stack) {
        return new HologramScreen(pos, stack);
    }

    public static BlockHoloProjectorSoundInstance playerHoloSound(BlockPos pos, Supplier<Boolean> activeCheck) {
        return new BlockHoloProjectorSoundInstance(pos, activeCheck);
    }
}
