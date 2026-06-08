package mod.syconn.svc.client;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.client.screen.HologramScreen;
import mod.syconn.svc.client.sounds.HoloProjectorSoundInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ClientHooks {

    public static Screen createHologramScreen(BlockPos pos, @Nullable ItemStack stack) {
        return new HologramScreen(pos, stack);
    }

    public static HoloProjectorSoundInstance playerHoloSound(BlockPos pos, Supplier<Boolean> activeCheck) {
        return new HoloProjectorSoundInstance(pos, activeCheck);
    }
}
