package mod.syconn.svc.utils.generic;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.NativeImage;
import dev.architectury.utils.GameInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;

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
        if (FontUtil.isValidPlayerName(name)) {
            if (!PROFILE_CACHE.containsKey(name)) {
                SkullBlockEntity.updateGameprofile(new GameProfile(Util.NIL_UUID, name), profile -> handleProfileCache(profile, profileHandler));
            } else profileHandler.accept(PROFILE_CACHE.get(name));
        } else profileHandler.accept(new GameProfile(Util.NIL_UUID, name));
    }

    public static void loadGameProfile(String name, Runnable callback) {
        name = name.toLowerCase();
        if (FontUtil.isValidPlayerName(name) && !PROFILE_CACHE.containsKey(name)) SkullBlockEntity.updateGameprofile(new GameProfile(Util.NIL_UUID, name), profile -> handleProfileCache(profile, p -> callback.run()));
    }

    private static void handleProfileCache(GameProfile profile, Consumer<GameProfile> profileHandler) {
        PROFILE_CACHE.put(profile.getName().toLowerCase(), profile);
        final var location = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(profile);
        loadSkin(location).ifPresent(nativeImage -> {
            SKIN_CACHE.put(location, nativeImage);
            profileHandler.accept(profile);
        });
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
        if (!isResolved(profile)) {
            getter.accept(profile, DefaultPlayerSkin.getSkinModelName(profile.getId()).equals("slim"), DefaultPlayerSkin.getDefaultSkin(profile.getId()));
            return;
        }
        Minecraft.getInstance().getSkinManager().registerSkins(profile, (type, resourceLocation, minecraftProfileTexture) -> {
            if (type == MinecraftProfileTexture.Type.SKIN) getter.accept(profile, minecraftProfileTexture.getMetadata("model") != null, resourceLocation);
        }, false);
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
        SKIN_CACHE.computeIfAbsent(new ResourceLocation("skins/" + id), p -> skin);
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