package mod.syconn.svc.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import mod.syconn.svc.utils.generic.ResourceUtil;
import net.minecraft.client.renderer.texture.HttpTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(HttpTexture.class)
public class HttpTextureMixin {

    @Final
    @Shadow
    private File file;

    @Inject(method = "loadCallback", at = @At(value = "HEAD"))
    private void loadCallbackInject(NativeImage image, CallbackInfo ci) {
        NativeImage copy = image.mappedCopy(op -> op);
        ResourceUtil.registerSkin(file.getName(), copy);
    }
}
