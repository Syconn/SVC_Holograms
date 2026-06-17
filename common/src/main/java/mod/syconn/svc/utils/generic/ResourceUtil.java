package mod.syconn.svc.utils.generic;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import dev.architectury.utils.GameInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.function.TriFunction;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ResourceUtil {

    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, NativeImage> SKIN_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, GameProfile> PROFILE_CACHE = new ConcurrentHashMap<>();

    public static void loadGameProfile(String name, Consumer<GameProfile> profileHandler) {
        name = name.toLowerCase();
        if (StringUtil.isValidPlayerName(name)) {
            if (!PROFILE_CACHE.containsKey(name)) {
                SkullBlockEntity.fetchGameProfile(name).thenAccept(optional -> optional.ifPresent(profile -> handleProfileCache(profile, profileHandler)));
            } else profileHandler.accept(PROFILE_CACHE.get(name));
        } else profileHandler.accept(new GameProfile(Util.NIL_UUID, name));
    }

    public static void loadGameProfile(String name, Runnable callback) {
        name = name.toLowerCase();
        if (StringUtil.isValidPlayerName(name) && !PROFILE_CACHE.containsKey(name)) SkullBlockEntity.fetchGameProfile(name).thenAccept(optional -> optional.ifPresent(profile -> handleProfileCache(profile, p -> callback.run())));
    }

    private static void handleProfileCache(GameProfile profile, Consumer<GameProfile> profileHandler) {
        PROFILE_CACHE.put(profile.getName().toLowerCase(), profile);
        Minecraft.getInstance().getSkinManager().getOrLoad(profile).thenAccept(skin -> loadSkin(skin.texture()).ifPresent(nativeImage -> {
            SKIN_CACHE.put(skin.texture(), nativeImage);
            profileHandler.accept(profile);
        }));
    }

    public static Collection<GameProfile> getPlayerProfiles() {
        return PROFILE_CACHE.values();
    }

    public static Optional<NativeImage> loadSkin(ResourceLocation skinLocation) {
        if (SKIN_CACHE.containsKey(skinLocation)) return Optional.of(SKIN_CACHE.get(skinLocation).mappedCopy(p -> p));
        if (GameInstance.getClient().getResourceManager().getResource(skinLocation).isPresent()) return loadResource(skinLocation);
        return Optional.empty();
    }

    private static Optional<NativeImage> loadResource(ResourceLocation location) {
        try {
            var inputStream = GameInstance.getClient().getResourceManager().open(location);
            var nativeImage = NativeImage.read(inputStream);
            inputStream.close();
            return Optional.of(nativeImage);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static void getOrLoadFromUsername(String name, TriConsumer<GameProfile, Boolean, ResourceLocation> getter) {
        name = name.toLowerCase();
        var profile = PROFILE_CACHE.get(name);
        if (profile != null) loadFromProfile(profile, getter);
        loadGameProfile(name, resolvedProfile -> loadFromProfile(resolvedProfile, getter));
    }

    public static void loadFromProfile(GameProfile profile, TriConsumer<GameProfile, Boolean, ResourceLocation> getter) {
        var insecureSkin = Minecraft.getInstance().getSkinManager().getInsecureSkin(profile);
        if (isResolved(profile)) getter.accept(profile, insecureSkin.model().equals(PlayerSkin.Model.SLIM), insecureSkin.texture());
        else Minecraft.getInstance().getSkinManager().getOrLoad(profile).thenAccept(skin -> getter.accept(profile, skin.model().equals(PlayerSkin.Model.SLIM), skin.texture()));
    }

    private static boolean isResolved(GameProfile profile) {
        return profile != null && profile.getId() != null && !profile.getProperties().get("textures").isEmpty();
    }

    public static ResourceLocation registerOrGet(String id, DynamicTexture texture) {
        if (TEXTURE_CACHE.containsKey(id.toLowerCase())) return updateTexture(TEXTURE_CACHE.get(id.toLowerCase()), texture);
        var resourceLocation = GameInstance.getClient().getTextureManager().register(id.toLowerCase(), texture);
        TEXTURE_CACHE.put(id.toLowerCase(), resourceLocation);
        return resourceLocation;
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
        System.out.println("RUNNING REGISTER " + id);

        SKIN_CACHE.computeIfAbsent(ResourceLocation.parse("skins/" + id), p -> skin);
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
}
