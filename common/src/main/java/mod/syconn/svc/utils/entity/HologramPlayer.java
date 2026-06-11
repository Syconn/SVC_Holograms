package mod.syconn.svc.utils.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;

public class HologramPlayer extends RemotePlayer {

    private boolean lastNetworkSwinging;

    public HologramPlayer(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    public void handleNetworkSwing(boolean swinging, InteractionHand hand) {
        if (swinging && !lastNetworkSwinging) this.swing(hand);
        lastNetworkSwinging = swinging;
    }

    public void animationTick() {
        this.updateSwimming();
        this.updateSwinging();
    }

    private void updateSwinging() {
        this.oAttackAnim = this.attackAnim;
        int i = this.getCurrentSwingDuration();
        if (this.swinging) {
            this.swingTime++;
            if (this.swingTime >= i) {
                this.swingTime = 0;
                this.swinging = false;
            }
        } else this.swingTime = 0;

        this.attackAnim = (float) swingTime / i;
    }

    private int getCurrentSwingDuration() {
        if (MobEffectUtil.hasDigSpeed(this)) {
            return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(this));
        } else {
            return this.hasEffect(MobEffects.DIG_SLOWDOWN) ? 6 + (1 + this.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2 : 6;
        }
    }
}
