package mod.syconn.svc.client.sounds;

import mod.syconn.svc.core.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class ItemHoloProjectorSoundInstance extends AbstractTickableSoundInstance {

    private final Player player;

    public ItemHoloProjectorSoundInstance(Player player) {
        super(ModSounds.HOLOGRAM_STATIC.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.3F;
        this.x = player.getEyePosition().x;
        this.y = player.getEyePosition().y;
        this.z = player.getEyePosition().z;
        this.player = player;
        this.relative = false;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
    }

    @Override
    public void tick() {
        if (player.isRemoved()) {
            stop();
            return;
        }

        this.x = player.getEyePosition().x;
        this.y = player.getEyePosition().y;
        this.z = player.getEyePosition().z;
    }

    public void forceStop() {
        this.stop();
    }

    public BlockPos getPos() {
        return new BlockPos((int) this.x, (int) this.y, (int) this.z);
    }
}
