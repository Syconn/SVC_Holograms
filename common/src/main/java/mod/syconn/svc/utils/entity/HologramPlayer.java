package mod.syconn.svc.utils.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;

public class HologramPlayer extends RemotePlayer {

    public HologramPlayer(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    public void animationTick() {
        this.updateSwimming();
        this.updateSwingTime();
    }
}
