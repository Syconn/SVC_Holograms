package mod.syconn.svc.client.sounds;

import mod.syconn.svc.core.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class BlockHoloProjectorSoundInstance extends AbstractTickableSoundInstance {

    private final Supplier<Boolean> activeCheck;

    public BlockHoloProjectorSoundInstance(BlockPos pos, Supplier<Boolean> activeCheck) {
        super(ModSounds.HOLOGRAM_STATIC.get(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.7F;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
        this.activeCheck = activeCheck;
        this.relative = false;
        this.attenuation = Attenuation.LINEAR;
    }


    @Override
    public void tick() {
        if (!activeCheck.get()) this.stop();
    }

    public void forceStop() {
        this.stop();
    }
}
