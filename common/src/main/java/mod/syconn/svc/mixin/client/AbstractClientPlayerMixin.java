package mod.syconn.svc.mixin.client;

import mod.syconn.svc.utils.interfaces.IHologramEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin implements IHologramEntity {

    @Unique
    private boolean svc$hologram;

    @Override
    public boolean svc$isHologram() {
        return svc$hologram;
    }

    @Override
    public void svc$setHologram(boolean hologram) {
        this.svc$hologram = hologram;
    }
}
