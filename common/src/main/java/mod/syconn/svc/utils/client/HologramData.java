package mod.syconn.svc.utils.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import mod.syconn.svc.client.ClientRenderSystem;
import mod.syconn.svc.client.SVCClient;
import mod.syconn.svc.client.render.entity.HologramRenderer;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.generic.AnimationUtil;
import mod.syconn.svc.utils.generic.ColorUtil;
import mod.syconn.svc.utils.generic.MathUtil;
import mod.syconn.svc.utils.generic.ResourceUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class HologramData {

    public static final byte TRANSITION_TICKS = 16;
    private final int textureHeight = 64;
    private long lastUpdateTime = System.currentTimeMillis();
    private boolean activeRender;
    private AbstractClientPlayer player;
    private UUID uniqueID;
    private GameProfile profile;
    private HologramRenderer renderer;
    private ResourceLocation skin;
    private ResourceLocation skinPath;
    private Vec3 currentPosition;
    private Vec3 previousPosition;
    private Runnable endCall = null;
    private boolean staticRender = false;
    private boolean textureLoaded = false;
    private int tickWait = 0;
    private int transition;
    private int scanBarTicks = 0;
    private int scanBar1 = 0;
    private int scanBar2 = 16;

    public HologramData(UUID uuid, UUID uniqueID, Vec3 currentPosition) {
        final var info = getInfo(uuid);
        this.profile = info.getProfile();
        this.uniqueID = uniqueID;
        this.transition = TRANSITION_TICKS;
        this.activeRender = true;
        this.currentPosition = currentPosition;
        this.previousPosition = currentPosition;
        ResourceUtil.loadSkin(info.getSkin().texture()).ifPresent(skin -> loadSkinAsync(profile, info.getSkin().model().equals(PlayerSkin.Model.SLIM), info.getSkin().texture(), skin));
    }

    public HologramData(UUID uuid, UUID uniqueID) {
        final var info = getInfo(uuid);
        this.profile = info.getProfile();
        this.uniqueID = uniqueID;
        this.transition = TRANSITION_TICKS;
        this.staticRender = true;
        this.activeRender = true;
        this.currentPosition = new Vec3(0, 0, 0);
        this.previousPosition = new Vec3(0, 0, 0);
        ResourceUtil.loadSkin(info.getSkin().texture()).ifPresent(skin -> loadSkinAsync(profile, info.getSkin().model().equals(PlayerSkin.Model.SLIM), info.getSkin().texture(), skin));
    }

    public HologramData(String name, UUID uniqueID) {
        generateInformationByName(name, uniqueID);
    }

    public HologramData generateInformationByName(String name, UUID uniqueID) {
        if (name.isEmpty()) {
            endCall(this::onEnd);
            return this;
        }

        this.uniqueID = uniqueID;
        this.staticRender = true;
        this.profile = new GameProfile(Util.NIL_UUID, name);
        this.transition = TRANSITION_TICKS;
        this.activeRender = true;
        ResourceUtil.getOrLoadFromUsername(name, (profile, slim, location) -> ResourceUtil.loadSkin(location).ifPresent(skin -> loadSkinAsync(profile, slim, location, skin)));
        return this;
    }

    private PlayerInfo getInfo(UUID uuid) {
        return Objects.requireNonNull(Minecraft.getInstance().player).connection.getPlayerInfo(uuid);
    }

    private void loadSkinAsync(GameProfile profile, boolean slim, ResourceLocation skinPath, NativeImage image) {
        this.profile = profile;
        this.skinPath = skinPath;
        this.player = Minecraft.getInstance().level == null ? null : new AbstractClientPlayer(Minecraft.getInstance().level, profile) {};
        this.renderer = new HologramRenderer(this, slim);
        final var texture = new DynamicTexture(image);
        ResourceUtil.modifyTexture(texture, this::getPixelColor);
        this.skin = ResourceUtil.registerOrGet(profile.getName() + uniqueID, texture);
        this.textureLoaded = true;
    }

    private void onEnd() {
        this.player = null;
        this.renderer = null;
        this.profile = null;
        this.skin = null;
    }

    public void tick() {
        if (!this.activeRender() || !this.textureLoaded) return;

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
                this.skin = ResourceUtil.registerOrGet(profile.getName() + uniqueID, texture.get());
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
        final var time = SVCClient.getTickDelta() + (player != null ? player.tickCount : 0);
        int finalAlpha = scanBar(y) ? 255 : Mth.clamp((int)(160.0f * (0.75f + 0.20f * Mth.sin(time * 0.25f) + 0.05f * Mth.sin(time * 1.37f))), 0, 255);
        return FastColor.ABGR32.color(finalAlpha, scanBar(y) ? ColorUtil.packArgb(192, 192, 192, 100) : ColorUtil.hologramColor(rgba));
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

    public void endCall() {
        if (this.endCall == null && this.activeRender) {
            this.endCall = () -> activeRender = false;
            this.transition = -TRANSITION_TICKS;
        }
    }

    public void endCall(Runnable runnable) {
        if (this.endCall == null) {
            this.endCall = runnable;
            this.transition = -TRANSITION_TICKS;
        }
    }

    public void setPosition(Vec3 position) {
        if (!position.equals(this.currentPosition)) {
            this.previousPosition = this.getInterpolatedPosition();
            this.currentPosition = position;
            this.lastUpdateTime = System.currentTimeMillis();
        }
    }

    public Vec3 getInterpolatedPosition() {
        return previousPosition == null ? new Vec3(0, 0, 0) : previousPosition.lerp(currentPosition, Math.min(1.0, (System.currentTimeMillis() - lastUpdateTime) / 100.0));
    }

    private boolean scanBar(int y) {
        return this.scanBar1 == y || MathUtil.wrap(this.scanBar1 - 32, this.textureHeight) == y || this.scanBar2 == y || MathUtil.wrap(this.scanBar2 - 32, this.textureHeight) == y;
    }

    public HologramData setActiveRender(boolean activeRender, Vec3 pos) {
        this.setPosition(pos);
        if (this.activeRender == activeRender) return this;
        this.activeRender = activeRender;
        if (this.activeRender) this.transition = TRANSITION_TICKS;
        return this;
    }

    public boolean shouldRender() {
        return Constants.RANDOM.nextInt(30) != 0 && this.player != null && textureLoaded;
    }

    public boolean activeRender() {
        if (profile != null && !staticRender) this.player = ClientRenderSystem.get().getPlayer(this.profile.getId());
        return this.player != null && this.skin != null && activeRender;
    }

    public GameProfile getProfile() {
        return profile == null ? this.profile = new GameProfile(Util.NIL_UUID, "") : profile; // WHYYYY
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

    public boolean isTextureLoaded() {
        return textureLoaded;
    }
}
