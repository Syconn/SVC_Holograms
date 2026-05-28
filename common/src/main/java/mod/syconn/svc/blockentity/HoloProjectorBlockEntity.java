package mod.syconn.svc.blockentity;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.utils.block.WorldPos;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class HoloProjectorBlockEntity extends SyncedBlockEntity {

    private UUID receiverUUID = null;

    public HoloProjectorBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.HOLO_PROJECTOR.get(), pWorldPosition, pBlockState);
        this.receiverUUID = UUID.randomUUID();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HoloProjectorBlockEntity blockEntity) {
        if (level instanceof ServerLevel serverLevel) {
            var network = HologramNetwork.get(serverLevel);
            var networkData = network.getBlockData(blockEntity.callId);
            var handheld = network.getHandheldPlayer(blockEntity.callId);
            if (networkData != null && !networkData.isEmpty() || handheld.isPresent()) {
                if (networkData != null && !networkData.isEmpty()) {
                    var update = false;
                    var map = networkData.entrySet().stream().filter(e -> !e.getKey().equals(new WorldPos(level.dimension(), pos))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    var entities = map.values().stream().flatMap(Collection::stream).toList();
                    var removals = Map.copyOf(blockEntity.renderables).keySet().stream().filter(u -> !entities.contains(u)).toList();
                    if (!removals.isEmpty()) update = true;
                    removals.forEach(blockEntity.renderables::remove);
                    for (var entry : map.entrySet()) {
                        for (var uuid : entry.getValue()) {
                            if (!blockEntity.renderables.containsKey(uuid) || !blockEntity.renderables.get(uuid).equals(entry.getKey())) {
                                var player = level.getServer().getLevel(entry.getKey().level()).getPlayerByUUID(uuid);
                                blockEntity.renderables.put(uuid, player == null ? new Vec3(0, 0, 0) : player.position().subtract(entry.getKey().toVector()));
                                update = true;
                            }
                        }
                    }
                    if (update) blockEntity.markDirty();
                }
                if (handheld.isPresent() && !blockEntity.renderables.containsKey(handheld.get())) {
                    blockEntity.renderables.put(handheld.get(), new Vec3(0.5f, 0.12f, 0.5f));
                    blockEntity.markDirty();
                }
            } else if (!blockEntity.renderables.isEmpty()) {
                blockEntity.renderables.clear();
                blockEntity.callId = null;
                blockEntity.markDirty();
            }
        }
    }

    public UUID getReceiverUUID() {
        return receiverUUID;
    }

    @Override
    public void load(CompoundTag tag) {
        var renderables = NBTUtil.getMap(tag.getCompound("renderables"), NBTUtil::getUUID, NBTUtil::getVec3);
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> this.deletions.putAll(this.renderables.entrySet().stream()
                .filter(v -> !renderables.containsKey(v.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));

        this.renderables.clear();
        this.renderables.putAll(renderables);
        this.callId = NBTUtil.getNullable(tag.getCompound("call"), NBTUtil::getUUID);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("call", NBTUtil.putNullable(this.callId, NBTUtil::putUUID));
    }
}
