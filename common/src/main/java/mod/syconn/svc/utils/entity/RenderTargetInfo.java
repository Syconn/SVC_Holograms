package mod.syconn.svc.utils.entity;

import mod.syconn.svc.client.ClientRenderSystem;
import mod.syconn.svc.utils.interfaces.IExtraRenderInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Inspired by RenderDistantPlayers Mod **/
public class RenderTargetInfo {

    private final UUID uuid;
    private final String entityTypeId;
    private String name;
    private Vec3 pos;
    private float xRot, yRot;
    private long lastUpdateTime = System.currentTimeMillis();
    @Nullable private Entity entity;
    @Nullable private IExtraRenderInfo extraInfo;
    private String prevExtraInfoId = "";
    private int age = 0;

    public RenderTargetInfo(Entity target) {
        uuid = target.getUUID();
        name = target.getScoreboardName();
        entityTypeId = EntityType.getKey(target.getType()).toString();
        pos = target.position();
        xRot = target.getXRot();
        yRot = target.getYRot();
        updateExtraInfo();
        if (extraInfo != null) extraInfo.getInfoServerSide(target);
    }

    public RenderTargetInfo(FriendlyByteBuf buffer) {
        uuid = buffer.readUUID();
        name = buffer.readUtf();
        var px = buffer.readFloat();
        var py = buffer.readFloat();
        var pz = buffer.readFloat();
        pos = new Vec3(px, py, pz);
        xRot = buffer.readFloat();
        yRot = buffer.readFloat();
        entityTypeId = buffer.readUtf();
        updateExtraInfo();
        if (extraInfo != null) extraInfo.getInfoClientSide(buffer);
    }

    public void tickFakeEntity(@NotNull Entity entity) {
        if (extraInfo != null) extraInfo.tickFakeEntity(entity);
        if (entity instanceof HologramPlayer hp) hp.animationTick();
        ++age;
        if (age > 0) entity.setOldPosAndRot();
    }

    public void updateFakeEntity(@NotNull Entity entity) { // TODO REMOVE DELTA MOVE
        if (age > 0) entity.setOldPosAndRot();
        entity.setPos(getPos());
        entity.setXRot(getXRot());
        entity.setYRot(getYRot());
        if (extraInfo != null) extraInfo.updateFakeEntity(entity);
        if (age == 0) entity.setOldPosAndRot();
    }

    @Nullable
    public Entity getFakeEntity(ClientRenderSystem clientManager) {
        if (didEntityChange()) {
            entity = clientManager.createFakePlayer(this);
            return entity;
        }
        return entity;
    }

    private boolean didEntityChange() {
        if (entity == null) return true;
        var type = EntityType.getKey(entity.getType()).toString();
        return !type.equals(entityTypeId);
    }

    public void update(RenderTargetInfo newest, ClientRenderSystem clientManager) {
        lastUpdateTime = System.currentTimeMillis();
        name = newest.name;
        pos = newest.pos;
        xRot = newest.xRot;
        yRot = newest.yRot;
        extraInfo = newest.extraInfo;
        var fake = getFakeEntity(clientManager);
        if (fake != null) updateFakeEntity(fake);
    }

    private void updateExtraInfo() {
        var id = getEntityTypeId();
        if (extraInfo == null || !id.equals(prevExtraInfoId)) {
            extraInfo = ExtraRenderInfoManager.get(id);
            prevExtraInfoId = id;
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(uuid);
        buffer.writeUtf(name);
        buffer.writeFloat((float)pos.x);
        buffer.writeFloat((float)pos.y);
        buffer.writeFloat((float)pos.z);
        buffer.writeFloat(xRot);
        buffer.writeFloat(yRot);
        buffer.writeUtf(entityTypeId);
        if (extraInfo != null) extraInfo.encodeInfoServerSide(buffer);
    }

    public long getLastUpdateTime(){
        return lastUpdateTime;
    }

    public Vec3 getPos() {
        return pos;
    }

    public float getXRot() {
        return xRot;
    }

    public float getYRot() {
        return yRot;
    }

    public String getEntityTypeId() {
        return entityTypeId;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public IExtraRenderInfo getExtraInfo() {
        return extraInfo;
    }

    @Override
    public String toString() {
        return "RenderTargetInfo:"+name+":"+entityTypeId;
    }
}
