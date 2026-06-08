package mod.syconn.svc.blockentity;

import mod.syconn.svc.client.ClientHooks;
import mod.syconn.svc.client.SVCClient;
import mod.syconn.svc.client.sounds.HoloProjectorSoundInstance;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.core.ModSounds;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.client.ParticleEvent;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import oshi.hardware.SoundCard;

import java.util.*;

public class HoloProjectorBlockEntity extends SyncedBlockEntity {

    private List<ParticleEvent> particleQueue = new ArrayList<>();
    private Map<UUID, Vec3> renderables = new HashMap<>();
    private boolean active;
    private String soloRender = "";
    private double rotation = 0;
    private UUID receiverUUID;
    private HoloProjectorSoundInstance soundInstance;
    private boolean wasActive;

    public HoloProjectorBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.HOLO_PROJECTOR.get(), pWorldPosition, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HoloProjectorBlockEntity blockEntity) {
        if (level instanceof ServerLevel serverLevel && blockEntity.receiverUUID != null) {
            final var network = HologramNetwork.get(serverLevel);
            final var callData = network.getBlockReceiver(blockEntity.receiverUUID);

            if (callData != null && callData.callID != null) {
                blockEntity.active = true;
                if (!blockEntity.soloRender.isEmpty()) blockEntity.soloRender = "";
                final var players = level.getEntitiesOfClass(Player.class, new AABB(pos).move(0, 1, 0).inflate(3.5));
                final var renderMembers = new HashMap<UUID, Vec3>();
                final var call = network.getCall(callData.callID);
                blockEntity.renderables.clear();
                if (call.renderMembers.containsKey(blockEntity.getReceiverUUID())) {
                    for (var entry : call.renderMembers.entrySet())
                        if (entry.getKey() != blockEntity.receiverUUID)
                            blockEntity.renderables.putAll(entry.getValue());
                }
                for (Player player : players) renderMembers.put(player.getUUID(), player.position().subtract(pos.getCenter()));
                network.setRenderMembers(callData.callID, blockEntity.getReceiverUUID(), renderMembers);

                for (var entry : call.callers.entrySet()) {
                    if (entry.getValue().type == CallData.ReceiverType.ITEM) {
                        blockEntity.renderables.put(entry.getKey(), new Vec3(0, -0.3f, 0));
                        break;
                    }
                }
                blockEntity.markDirty();
            } else if (callData != null && !blockEntity.renderables.isEmpty()) {
                blockEntity.renderables.clear();
                blockEntity.markDirty();
            }

            if (!blockEntity.getSoloRender().isEmpty()) {
                blockEntity.active = true;
                blockEntity.markDirty();
            }

            if (blockEntity.getSoloRender().isEmpty() && (callData == null || callData.callID == null)) {
                blockEntity.active = false;
                blockEntity.markDirty();
            }
        } else if (level instanceof ClientLevel) {
            if (!blockEntity.particleQueue.isEmpty()) {
                for (var event : blockEntity.particleQueue) level.addParticle(event.type(), event.pos().x, event.pos().y, event.pos().z, event.velocity().x, event.velocity().y, event.velocity().z);
                blockEntity.particleQueue.clear();
                blockEntity.markDirty();
            }

            boolean active = blockEntity.isActive();
            if (active && !blockEntity.wasActive) {
                blockEntity.soundInstance = ClientHooks.playerHoloSound(pos, blockEntity::isActive);
                Minecraft.getInstance().getSoundManager().play(blockEntity.soundInstance);
                level.playLocalSound(pos, ModSounds.HOLOGRAM_ACTIVATE.get(), SoundSource.BLOCKS, 0.6f, 1.0F, false);
            } else if (!active && blockEntity.wasActive && blockEntity.soundInstance != null) {
                blockEntity.soundInstance.forceStop();
                blockEntity.soundInstance = null;
                level.playLocalSound(pos, ModSounds.HOLOGRAM_DEACTIVATE.get(), SoundSource.BLOCKS, 0.6f, 1.0F, false);
            }
            blockEntity.wasActive = active;
        }
    }

    public HoloProjectorSoundInstance getSoundInstance() {
        return soundInstance;
    }

    public void addParticleEvent(ParticleEvent event) {
        particleQueue.add(event);
        this.markDirty();
    }

    public boolean isActive() {
        return active;
    }

    public Map<UUID, Vec3> getRenderables() {
        return renderables;
    }

    public void setSoloRender(String soloRender, Vec3 pos) {
        this.soloRender = soloRender;
        this.active = !soloRender.isEmpty();

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
        this.particleQueue = NBTUtil.getList(tag.getCompound("queue"), ParticleEvent::from);
        this.active = tag.getBoolean("active");
        if (tag.contains("renderables")) this.renderables = NBTUtil.getMap(tag.getCompound("renderables"), NBTUtil::getUUID, NBTUtil::getVec3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("receiverUUID", NBTUtil.putNullable(this.receiverUUID, NBTUtil::putUUID));
        tag.put("soloRender", NBTUtil.putNullable(this.soloRender, s -> NBTUtil.convert(t -> t.putString("", s))));
        tag.putDouble("rotation", this.rotation);
        tag.put("queue", NBTUtil.putList(this.particleQueue, ParticleEvent::save));
        tag.putBoolean("active", this.active);
    }

    @Override
    protected void saveSyncData(CompoundTag tag) {
        this.saveAdditional(tag);
        tag.put("renderables", NBTUtil.putMap(this.renderables, NBTUtil::putUUID, NBTUtil::putVec3));
    }
}
