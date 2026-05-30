package mod.syconn.svc.utils.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.client.render.entity.HologramRenderer;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.generic.AnimationUtil;
import mod.syconn.svc.utils.generic.ColorUtil;
import mod.syconn.svc.utils.generic.MathUtil;
import mod.syconn.svc.utils.generic.ResourceUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

import static mod.syconn.svc.utils.generic.ResourceUtil.getPlayerInfoFromName;

@Environment(EnvType.CLIENT)
public class HologramData {

    public static final byte TRANSITION_TICKS = 16;
    private final AbstractClientPlayer player;
    private final boolean item;
    private final boolean staticRender;
    private final int textureHeight = 64;
    private HologramRenderer renderer;
    private ResourceLocation skin;
    private ResourceLocation skinPath;
    private Vec3 currentPosition;
    private Vec3 previousPosition;
    private long lastUpdateTime = System.currentTimeMillis();
    private Runnable endCall = null;
    private int tickWait = 0;
    private int transition;
    private int scanBarTicks = 0;
    private int scanBar1 = 0;
    private int scanBar2 = 16;

//    public HologramData(@NotNull UUID uuid, Vec3 currentPosition, boolean item) {
//        final var playerInfo = getPlayerInfo(uuid);
//        final var clientPlayer = item ? null : minecraft.level.getPlayerByUUID(playerInfo.getProfile().getId());
//        final var texture = ResourceUtil.loadSkin(playerInfo.getSkinLocation()).map(DynamicTexture::new);
//        texture.ifPresent(dynamicTexture -> ResourceUtil.modifyTexture(dynamicTexture, this::getPixelColor));
//
//        this.item = item;
//        this.currentPosition = currentPosition;
//        this.previousPosition = currentPosition;
//        this.renderer = new HologramRenderer(this, playerInfo.getModelName().equals("slim"));
//        this.player = clientPlayer != null ? (AbstractClientPlayer) clientPlayer : new AbstractClientPlayer(minecraft.level, playerInfo.getProfile()) {};
//        this.skin = texture.map(dynamicTexture -> ResourceUtil.registerOrGet(playerInfo.getProfile().getName(), dynamicTexture)).orElse(playerInfo.getSkinLocation());
//        this.transition = TRANSITION_TICKS;
//    }

    public HologramData(String name) {
        final var level = Minecraft.getInstance().level;
        final var playerInfo = getPlayerInfoFromName(name);

        this.staticRender = true;
        this.item = false;
        this.renderer = new HologramRenderer(this, ResourceUtil.getModel(name));
        this.player = level == null ? null : new AbstractClientPlayer(level, playerInfo.getProfile()) {};
        this.skin = DefaultPlayerSkin.getDefaultSkin();
        this.skinPath = this.skin;
        this.transition = TRANSITION_TICKS;

        SkullBlockEntity.updateGameprofile(new GameProfile(null, name), this::loadSkinAsync);
    }

    private void loadSkinAsync(GameProfile profile) {
        var map = Minecraft.getInstance().getSkinManager().getInsecureSkinInformation(profile);
        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) this.skin = Minecraft.getInstance().getSkinManager().registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
        this.skinPath = this.skin;

        final var texture = ResourceUtil.loadSkin(this.skinPath).map(DynamicTexture::new);
        texture.ifPresent(dynamicTexture -> ResourceUtil.modifyTexture(dynamicTexture, this::getPixelColor));
        this.skin = texture.map(dynamicTexture -> ResourceUtil.registerOrGet(profile.getName(), dynamicTexture)).orElse(this.skinPath);
    }

    private PlayerInfo getPlayerInfo(UUID uuid) {
        final var player = GameInstance.getClient().player;
        if (player == null || player.connection.getPlayerInfo(uuid) == null) return new PlayerInfo(new GameProfile(uuid, "Offline-Player"), false);
        return player.connection.getPlayerInfo(uuid);
    }

    public void tick() {
        tickWait++;
        if (tickWait < 4) return;
        tickWait = 0;

        if (this.transition > 0) this.transition--;
        if (this.transition < 0) this.transition++;

        this.scanBarTicks++;
        if (this.scanBarTicks >= 2) {
            this.scanBarTicks = 0;
            final var texture = ResourceUtil.loadSkin(this.skinPath).map(DynamicTexture::new);
            if (texture.isPresent()) {
                if (this.scanBar1 >= this.textureHeight) this.scanBar1 = 0;
                if (this.scanBar2 >= this.textureHeight) this.scanBar2 = 0;
                ResourceUtil.modifyTexture(texture.get(), this::getPixelColor);
                ResourceUtil.registerOrGet(player.getName().getString(), texture.get());
                this.scanBar1++;
                this.scanBar2++;
            }
        }
    }

    public boolean isStaticRender() {
        return staticRender;
    }

    private int getPixelColor(int x, int y, int rgba) {
        if (FastColor.ARGB32.alpha(rgba) == 0) return rgba;
        return FastColor.ABGR32.color(scanBar(y) ? 255 : 160, scanBar(y) ? ColorUtil.packArgb(192, 192, 192, 100) : ColorUtil.hologramColor(rgba));
    }

    public float getAnimationScale(float partialTicks) {
        if (this.transition == 0) {
            var ret = this.endCall != null ? 0 : 1;
            if (this.endCall != null) {
                this.endCall.run();
                this.endCall = null;
            }
            return ret;
        }
        if (this.transition > 0) return AnimationUtil.outCubic(1 - (this.transition - partialTicks) / TRANSITION_TICKS);
        return AnimationUtil.inCubic(-(this.transition + partialTicks) / TRANSITION_TICKS);
    }

    public void endCall(Runnable endCall) {
        if (this.endCall == null) {
            this.endCall = endCall;
            this.transition = -TRANSITION_TICKS;
        }
    }

//    public HologramData setPosition(Vec3 position) {
//        if (!position.equals(this.currentPosition)) {
//            this.previousPosition = this.getInterpolatedPosition();
//            this.currentPosition = position;
//            this.lastUpdateTime = System.currentTimeMillis();
//        }
//        return this;
//    }

    public Vec3 getCurrentPosition() {
        return currentPosition == null ? new Vec3(0, 0, 0) : currentPosition;
    }

    public Vec3 getInterpolatedPosition() {
        return previousPosition == null ? new Vec3(0, 0, 0) : previousPosition.lerp(currentPosition, Math.min(1.0, (System.currentTimeMillis() - lastUpdateTime) / 100.0));
    }

    public int getTransition() {
        return transition;
    }

    private boolean scanBar(int y) {
        return this.scanBar1 == y || MathUtil.wrap(this.scanBar1 - 32, this.textureHeight) == y || this.scanBar2 == y || MathUtil.wrap(this.scanBar2 - 32, this.textureHeight) == y;
    }

    public boolean shouldRender() {
        return Constants.RANDOM.nextInt(55) != 0;
    }

    public boolean isItem() {
        return item;
    }

    public HologramRenderer getRenderer() {
        return renderer;
    }

    public AbstractClientPlayer getPlayer() {
        return player;
    }

    public ResourceLocation getSkin() {
        return skin;
    }

//    public static class HologramTag {
//
//        private static final String ID = "hologramData";
//
//        public final UUID itemId;
//        public UUID uuid;
//
//        public HologramTag(@Nullable UUID uuid) {
//            this.uuid = uuid;
//            this.itemId = UUID.randomUUID();
//        }
//
//        public HologramTag(CompoundTag tag) {
//            this.uuid = tag.hasUUID("uuid") ? tag.getUUID("uuid") : null;
//            this.itemId = tag.hasUUID("id") ? tag.getUUID("id") : UUID.randomUUID();
//        }
//
//        private CompoundTag save() {
//            var tag = new CompoundTag();
//            if (this.uuid != null) tag.putUUID("uuid", this.uuid);
//            tag.putUUID("id", this.itemId);
//            return tag;
//        }
//
//        public static HologramTag getOrCreate(ItemStack stack) {
//            var tag = !stack.getOrCreateTag().contains(ID) ? create() : new HologramTag(stack.getOrCreateTag().getCompound(ID));
//            tag.change(stack);
//            return tag;
//        }
//
////        public static void update(ItemStack stack, UUID uuid) {
////            var holo = getOrCreate(stack);
////            holo.uuid = uuid;
////            holo.change(stack);
////        }
//
//        private static HologramTag create() {
//            return new HologramTag((UUID) null);
//        }
//
//        public void change(ItemStack stack) {
//            stack.getOrCreateTag().put(ID, save());
//        }
//    }
}
