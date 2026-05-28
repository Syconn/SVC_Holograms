package mod.syconn.svc.utils.generic;

import com.mojang.blaze3d.platform.NativeImage;
import dev.architectury.utils.GameInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.function.TriFunction;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ResourceUtil {

    private static final Map<String, ResourceLocation> DYNAMIC_TEXTURES = new HashMap<>();
    private static final Map<ResourceLocation, NativeImage> SKINS = new HashMap<>();

    public static Optional<NativeImage> loadResource(ResourceLocation location) {
        try {
            var inputStream = GameInstance.getClient().getResourceManager().open(location);
            var nativeImage = NativeImage.read(inputStream);
            inputStream.close();
            return Optional.of(nativeImage);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static Optional<NativeImage> loadSkin(ResourceLocation skinLocation) {
        if (GameInstance.getClient().getResourceManager().getResource(skinLocation).isPresent()) return loadResource(skinLocation);
        if (SKINS.containsKey(skinLocation)) return Optional.of(SKINS.get(skinLocation).mappedCopy(p -> p));
        return Optional.empty();
    }

    public static ResourceLocation registerOrGet(String id, DynamicTexture texture) {
        if (DYNAMIC_TEXTURES.containsKey(id.toLowerCase())) return updateTexture(DYNAMIC_TEXTURES.get(id.toLowerCase()), texture);
        var resourceLocation = GameInstance.getClient().getTextureManager().register(id.toLowerCase(), texture);
        DYNAMIC_TEXTURES.put(id.toLowerCase(), resourceLocation);
        return resourceLocation;
    }

    private static ResourceLocation updateTexture(ResourceLocation loaded, DynamicTexture target) {
        var resource = GameInstance.getClient().getTextureManager().getTexture(loaded);
        if (resource instanceof DynamicTexture texture && target.getPixels() != null) {
            for (int x = 0; x < texture.getPixels().getWidth(); x++) {
                for (int y = 0; y < texture.getPixels().getHeight(); y++) {
                    texture.getPixels().setPixelRGBA(x, y, target.getPixels().getPixelRGBA(x, y));
                }
            }

            texture.upload();
        }
        return loaded;
    }

    public static void modifyTexture(DynamicTexture texture, TriFunction<Integer, Integer, Integer, Integer> function) {
        var image = texture.getPixels();
        if (image != null) {
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    image.setPixelRGBA(x, y, function.apply(x, y, image.getPixelRGBA(x, y)));
                }
            }
            texture.upload();
        }
    }

    public static void registerSkin(String id, NativeImage skin) {
        var path = new ResourceLocation("skins/" + id);
        if (!SKINS.containsKey(path)) SKINS.put(path, skin);
    }
}
