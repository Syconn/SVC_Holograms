package mod.syconn.svc.mixin.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import mod.syconn.svc.utils.generic.ResourceUtil;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.UUID;

@Mixin(HttpTexture.class)
public class HttpTextureMixin {

    @Final
    @Shadow
    private File file;

    @Inject(method = "loadCallback", at = @At(value = "HEAD"), order = 200)
    private void loadCallbackInject(NativeImage image, CallbackInfo ci) { // TODO Essentials Kills this
        if (image == null) return;
        else {
            NativeImage copy = image.mappedCopy(op -> op);
            ResourceUtil.registerSkin(file.getName(), copy);
        }
    }
}
