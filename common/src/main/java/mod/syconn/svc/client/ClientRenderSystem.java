package mod.syconn.svc.client;

import com.mojang.authlib.GameProfile;
import mod.syconn.svc.utils.entity.RenderTargetInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientRenderSystem {

    private static ClientRenderSystem INSTANCE;
    private static final long MAX_TARGET_AGE = 500;
    private final Map<UUID, RenderTargetInfo> targets = new HashMap<>();

    public void handleRenderPlayerPacket(RenderTargetInfo info) {
        Minecraft m = Minecraft.getInstance();
        if (m.level == null) return;
        //        if (m.level.getEntity(info.getId()) != null) return; TODO PROBABLY CAUSE PLAYER SHOULD ONLY BE RENDERED IF NOT I PROXIMITY

        if (!this.targets.containsKey(info.getUUID())) this.targets.put(info.getUUID(), info);
        this.targets.get(info.getUUID()).update(info, this);
    }

    public RemotePlayer getPlayer(UUID uuid) {
        var info = this.targets.get(uuid);
        if (info == null) return null;
        return (RemotePlayer) info.getFakeEntity(this);
    }

//    public void renderTargets(PoseStack poseStack, Camera camera, float partialTick) {
//        poseStack.pushPose();
//        Minecraft m = Minecraft.getInstance();
//        MultiBufferSource.BufferSource buffer = m.renderBuffers().bufferSource();
//        this.targets.forEach((id, info) -> renderTarget(poseStack, camera, partialTick, info, buffer));
//        poseStack.popPose();
//    }

//    private void renderTarget(PoseStack poseStack, Camera camera, float partialTick, RenderTargetInfo info, MultiBufferSource.BufferSource buffer) {
//        Entity fake = info.getFakeEntity(this);
//        if (fake == null) return;
//        boolean valid = renderFakeEntity(fake, poseStack, camera, partialTick, buffer);
//        if (!valid) info.setInvalidEntityType();
//    }

//    private boolean renderFakeEntity(@NotNull Entity fake, PoseStack poseStack, Camera camera, float partialTick, MultiBufferSource.BufferSource buffer) {
//        Minecraft m = Minecraft.getInstance();
//        poseStack.pushPose();
//
//        int packedLight = m.getEntityRenderDispatcher().getPackedLightCoords(fake, partialTick);
//        double dx = Mth.lerp(partialTick, fake.xOld, fake.getX());
//        double dy = Mth.lerp(partialTick, fake.yOld, fake.getY());
//        double dz = Mth.lerp(partialTick, fake.zOld, fake.getZ());
//        float f = fake.getYRot();
//
//        Vec3 camPos = camera.getPosition();
//        Vec3 dist = new Vec3(dx, dy, dz).subtract(camPos);
////        float scale = (float) (renderRadius / dist.length());
//
////        poseStack.scale(scale, scale, scale);
//
//        Vec3 d = dist.normalize();
//
////        if (extra != null) d = extra.onRender(fake, poseStack, camera, f, d, partialTick, buffer, packedLight);
//
//        boolean valid = true;
//        try {
//            m.getEntityRenderDispatcher().render(fake, d.x, d.y, d.z, f, partialTick, poseStack, buffer, packedLight);
//        } catch (ReportedException e) {
//            System.out.println(e);
//        }
//
//        poseStack.popPose();
//        return valid;
//    }

    public void tick() {
        Minecraft m = Minecraft.getInstance();
        if (m.level == null) return;
        long currentTime = System.currentTimeMillis();

        this.targets.entrySet().removeIf(entry -> (currentTime - entry.getValue().getLastUpdateTime()) > MAX_TARGET_AGE); // TODO Removed Not in LocalRange from here

        this.targets.forEach((id, info) -> {
            Entity fake = info.getFakeEntity(this);
            if (fake != null) info.tickFakeEntity(fake);
        });
    }

    @Nullable
    public Entity createFakePlayer(RenderTargetInfo info) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        GameProfile profile = new GameProfile(info.getUUID(), info.getName());
        var player = new RemotePlayer(level, profile);
        if (info.getExtraInfo() != null) info.getExtraInfo().setupEntityOnCreate(player);
        return player;
    }

    private static void init() {
        INSTANCE = new ClientRenderSystem();
    }

    public static ClientRenderSystem get() {
        if (INSTANCE == null) init();
        return INSTANCE;
    }
}
