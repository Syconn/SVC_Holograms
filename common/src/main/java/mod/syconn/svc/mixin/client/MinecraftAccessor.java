package mod.syconn.svc.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
@Environment(EnvType.CLIENT)
public interface MinecraftAccessor {

    @Accessor("pausePartialTick")
    float getPausedTickDelta();
}
