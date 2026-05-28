package mod.syconn.svc.client.sounds;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.blockentity.HoloProjectorBlockEntity;
import mod.syconn.svc.core.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

@Environment(EnvType.CLIENT)
public class HoloProjectorSoundInstance extends AbstractTickableSoundInstance {

    public HoloProjectorSoundInstance(BlockPos pos) {
        super(ModSounds.HOLOGRAM_STATIC.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.1F;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }


    @Override
    public void tick() {
        var blockEntity = GameInstance.getClient().level.getBlockEntity(new BlockPos((int) this.x, (int) this.y, (int) this.z));
        if (!(blockEntity instanceof HoloProjectorBlockEntity) || ((HoloProjectorBlockEntity) blockEntity).getCallId() == null) {
            this.stop();
        }
    }
}
