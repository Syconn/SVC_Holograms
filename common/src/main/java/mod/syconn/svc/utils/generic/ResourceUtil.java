package mod.syconn.svc.utils.generic;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import dev.architectury.utils.GameInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.*;

@Environment(EnvType.CLIENT)
public class ResourceUtil {

    private static final Map<String, ResourceLocation> DYNAMIC_TEXTURES = new HashMap<>();
    private static final Map<ResourceLocation, NativeImage> SKINS = new HashMap<>();
    private static final Map<String, PlayerInfo> PLAYER_INFO = new HashMap<>();

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

    public static String convertUsernameToUUID(String name){
        try {
            HttpGet request = new HttpGet("https://api.mojang.com/users/profiles/minecraft/" + name);
            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            JsonObject jsonObject = (JsonObject) JsonParser.parseString(EntityUtils.toString(entity));
            return jsonObject.get("id").getAsString();
        } catch (IOException e) {
            return "";
        }
    }

    public static void getPlayerInfoFromName(String name) {
        if (PLAYER_INFO.containsKey(name)) return;
        PLAYER_INFO.put(name, new PlayerInfo(new GameProfile(untrimUUID(convertUsernameToUUID(name)), StringUtils.capitalize(name)), false));
    }

    public static Map<String, PlayerInfo> getAllInfo() {
        return PLAYER_INFO;
    }

    private static UUID untrimUUID(String trimmed) {
        return UUID.fromString(trimmed.substring(0, 8) + "-" + trimmed.substring(8, 12) + "-" + trimmed.substring(12, 16) + "-" + trimmed.substring(16, 20) + "-" + trimmed.substring(20));
    }
}
