package mod.syconn.svc.blockentity;

import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.utils.client.HologramData;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class HoloProjectorBlockEntity extends SyncedBlockEntity {

    private Map<UUID, Vec3> renderables = new HashMap<>();
    private String soloRender = "";
    private double rotation = 0;
    private UUID receiverUUID;

    public HoloProjectorBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.HOLO_PROJECTOR.get(), pWorldPosition, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HoloProjectorBlockEntity blockEntity) {
        if (level instanceof ServerLevel serverLevel && blockEntity.receiverUUID != null) {
            final var network = HologramNetwork.get(serverLevel);
            final var callData = network.getBlockReceiver(blockEntity.receiverUUID);
            if (callData != null && callData.callID != null) {
                if (!blockEntity.soloRender.isEmpty()) blockEntity.soloRender = "";
                final var players = level.getEntitiesOfClass(Player.class, new AABB(pos).move(0, 1, 0).inflate(3.5));
                final Set<UUID> uuids = new HashSet<>(players.size());
                for (Player player : players) uuids.add(player.getUUID());
                blockEntity.renderables.clear();
                for (var entry : network.getCall(callData.callID).renderMembers.entrySet()) if (!uuids.contains(entry.getKey())) blockEntity.renderables.put(entry.getKey(), entry.getValue());
                for (Player player : players) network.addNewRenderMember(callData.callID, player.getUUID(), player.position().subtract(pos.getCenter()));
                blockEntity.markDirty();
            } else if (callData != null && !blockEntity.renderables.isEmpty()) {
                blockEntity.renderables.clear();
                blockEntity.markDirty();
            }
        }
    }

    public Map<UUID, Vec3> getRenderables() {
        return renderables;
    }

    public void setSoloRender(String soloRender, Vec3 pos) {
        this.soloRender = soloRender;

        var pos2 = new Vec3(this.worldPosition.getX() + 0.5, 0, this.worldPosition.getZ() + 0.5);
        double dx = pos.x - pos2.x;
        double dz = pos.z - pos2.z;
        this.rotation = Math.toDegrees(Math.atan2(-dx, dz));

        this.markDirty();
    }

    public void setReceiverUUID(UUID receiverUUID) {
        this.receiverUUID = receiverUUID;
        this.markDirty();
    }

    public UUID getReceiverUUID() {
        return receiverUUID;
    }

    public String getSoloRender() {
        return soloRender;
    }

    public double getRotation() {
        return rotation;
    }

    @Override
    public void load(CompoundTag tag) {
        this.receiverUUID = NBTUtil.getNullable(tag.getCompound("receiverUUID"), NBTUtil::getUUID);
        this.soloRender = NBTUtil.getNullable(tag.getCompound("soloRender"), t -> t.getString(""));
        this.rotation = tag.getDouble("rotation");
        if (tag.contains("renderables")) this.renderables = NBTUtil.getMap(tag.getCompound("renderables"), NBTUtil::getUUID, NBTUtil::getVec3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("receiverUUID", NBTUtil.putNullable(this.receiverUUID, NBTUtil::putUUID));
        tag.put("soloRender", NBTUtil.putNullable(this.soloRender, s -> NBTUtil.convert(t -> t.putString("", s))));
        tag.putDouble("rotation", this.rotation);
    }

    @Override
    protected void saveSyncData(CompoundTag tag) {
        this.saveAdditional(tag);
        tag.put("renderables", NBTUtil.putMap(this.renderables, NBTUtil::putUUID, NBTUtil::putVec3));
    }
}
