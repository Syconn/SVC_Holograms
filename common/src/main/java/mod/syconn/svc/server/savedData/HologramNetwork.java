package mod.syconn.svc.server.savedData;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.core.ModSounds;
import mod.syconn.svc.network.Network;
import mod.syconn.svc.network.packets.client.MessagePlayerPacket;
import mod.syconn.svc.utils.block.WorldPos;
import mod.syconn.svc.utils.generic.ListUtil;
import mod.syconn.svc.utils.generic.MapUtil;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HologramNetwork extends SavedData {

    private static final String tagID = "hologram_network";

    private final Map<UUID, Call> CALLS = new HashMap<>();
    private final Map<UUID, UUID> ITEMS = new HashMap<>();
    private final Map<UUID, Map<WorldPos, List<UUID>>> BLOCKS = new HashMap<>();

    public HologramNetwork() { }

    public void createCall(Caller caller, List<Caller> callers) {
        if (this.CALLS.containsKey(caller.uuid)) this.leaveCall(caller.uuid, caller);
        this.CALLS.put(caller.uuid, new Call(caller.uuid, caller, callers.stream().collect(Collectors.toMap(Caller::uuid, c -> c))));
        if (caller.item != null) this.ITEMS.put(caller.item, caller.uuid);
        if (caller.location != null) {
            this.BLOCKS.put(caller.uuid, MapUtil.of(caller.location, ListUtil.empty()));
            GameInstance.getServer().getLevel(caller.location.level()).getBlockEntity(caller.location.pos(), ModBlockEntities.HOLO_PROJECTOR.get()).ifPresent(b -> b.addCall(caller.uuid));
        }
        this.setDirty();
        this.notifyPlayers(caller, callers);
    }

    public void connect(UUID callId, Caller caller) {
        this.CALLS.computeIfPresent(callId, ((uuid, call) -> call.updateParticipants(map -> map.put(caller.uuid, caller))));
        if (caller.item != null) this.ITEMS.put(caller.item, callId);
        if (caller.location != null) {
            this.BLOCKS.put(callId, MapUtil.add(caller.location, ListUtil.empty(), this.BLOCKS.get(callId)));
            GameInstance.getServer().getLevel(caller.location.level()).getBlockEntity(caller.location.pos(), ModBlockEntities.HOLO_PROJECTOR.get()).ifPresent(b -> b.addCall(callId));
        }
        this.setDirty();

        var call = this.CALLS.get(callId);
        var callers = ListUtil.add(call.owner, call.participants.values());
        for (var c : callers)
            if (c.location != null) GameInstance.getServer().getLevel(c.location.level()).playSound(null, c.location.pos(), ModSounds.HOLOGRAM_ACTIVATE.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
    }

    public void leaveCall(UUID callId, Caller caller) {
        var call = this.CALLS.get(callId);
        if (call != null) {
            if (caller.item != null) this.ITEMS.remove(caller.item);
            if (caller.location != null) {
                this.BLOCKS.put(callId, MapUtil.remove(caller.location, this.BLOCKS.get(callId)));
                GameInstance.getServer().getLevel(caller.location.level()).getBlockEntity(caller.location.pos(), ModBlockEntities.HOLO_PROJECTOR.get()).ifPresent(b -> b.addCall(null));
            }
            if (this.BLOCKS.get(callId) != null && this.BLOCKS.get(callId).isEmpty()) this.BLOCKS.remove(callId);

            if (call.owner.uuid.equals(caller.uuid)) {
                this.CALLS.remove(callId);
                call.participants.values().forEach(c -> {
                    if (c.item != null) this.ITEMS.remove(c.item);
                    if (caller.location != null && this.BLOCKS.containsKey(callId)) {
                        this.BLOCKS.put(callId, MapUtil.remove(caller.location, this.BLOCKS.get(callId)));
                        GameInstance.getServer().getLevel(caller.location.level()).getBlockEntity(caller.location.pos(), ModBlockEntities.HOLO_PROJECTOR.get()).ifPresent(b -> b.addCall(null));
                    }
                });
            } else if (this.CALLS.get(callId).participants().containsKey(caller.uuid)) this.CALLS.compute(callId, ((uuid, c) -> c.updateParticipants(map -> map.remove(caller.uuid))));

            if (this.CALLS.containsKey(callId) && this.CALLS.get(callId).participants.isEmpty()) this.leaveCall(callId, call.owner);
            this.setDirty();
        }
    }

    public void blockRemoved(UUID callId, WorldPos worldPos) {
        var call = this.CALLS.get(callId);
        if (call != null) {
            var others = Map.copyOf(call.participants);
            if (call.owner.location.equals(worldPos)) this.leaveCall(callId, call.owner);
            others.values().stream().filter(c -> worldPos.equals(c.location)).forEach(c -> this.leaveCall(callId, c));
        }
    }

    public void notifyPlayers(Caller caller, List<Caller> callers) {
        callers.stream().forEach(c -> {
            if (GameInstance.getServer() != null) {
                var owner = GameInstance.getServer().getPlayerList().getPlayer(caller.uuid);
                var serverPlayer = GameInstance.getServer().getPlayerList().getPlayer(c.uuid);
                if (serverPlayer != null && owner != null) Network.CHANNEL.sendToPlayer(serverPlayer, new MessagePlayerPacket(Component.literal(
                            "Incoming Transmission from " + owner.getName().getString()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
            }
        });
    }

    public Call getCall(UUID callId) {
        return this.CALLS.get(callId);
    }

    public List<Call> getCalls(UUID player) {
        return this.CALLS.values().stream().filter(call -> canJoinCall(player, call)).toList();
    }

    public UUID getCallId(UUID itemId) {
        return this.ITEMS.get(itemId);
    }

    public Map<WorldPos, List<UUID>> getBlockData(UUID callID) {
        return this.BLOCKS.get(callID);
    }

    public Optional<UUID> getHandheldPlayer(UUID callID) {
        var call = this.CALLS.get(callID);
        return call != null ? ListUtil.add(call.owner, call.participants.values()).stream().filter(c -> c.item != null).map(c -> c.uuid).findFirst() : Optional.empty();
    }

    private boolean canJoinCall(UUID player, Call call) {
        return call.owner.uuid.equals(player) || call.participants.containsKey(player);
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag) {
        compoundTag.put("calls", NBTUtil.putMap(this.CALLS, NBTUtil::putUUID, Call::save));
        compoundTag.put("items", NBTUtil.putMap(this.ITEMS, NBTUtil::putUUID, NBTUtil::putUUID));
        compoundTag.put("blocks", NBTUtil.putMap(this.BLOCKS, NBTUtil::putUUID, w -> NBTUtil.putMap(w, WorldPos::save, w2 -> NBTUtil.putList(w2, NBTUtil::putUUID))));
        return compoundTag;
    }

    private void read(CompoundTag tag) {
        this.CALLS.putAll(NBTUtil.getMap(tag.getCompound("calls"), NBTUtil::getUUID, Call::from));
        this.ITEMS.putAll(NBTUtil.getMap(tag.getCompound("items"), NBTUtil::getUUID, NBTUtil::getUUID));
        this.BLOCKS.putAll(NBTUtil.getMap(tag.getCompound("blocks"), NBTUtil::getUUID, t -> NBTUtil.getMap(t, WorldPos::from, t2 -> NBTUtil.getList(t2, NBTUtil::getUUID))));
    }

    public static HologramNetwork load(CompoundTag tag) {
        var network = create();
        network.read(tag);
        return network;
    }

    private static HologramNetwork create() {
        return new HologramNetwork();
    }

    public static HologramNetwork get(ServerLevel server) {
        return server.getDataStorage().computeIfAbsent(HologramNetwork::load, HologramNetwork::create, tagID);
    }

    public void playerLeave(ServerPlayer player) {
        if (this.CALLS.containsKey(player.getUUID())) this.leaveCall(player.getUUID(), this.CALLS.get(player.getUUID()).owner);

        var calls = Map.copyOf(this.CALLS);
        calls.forEach(((uuid, call) -> { if (call.participants.containsKey(player.getUUID())) this.leaveCall(uuid, call.participants.get(player.getUUID())); }));
    }

    public void serverTick(ServerLevel level) {
        cleanCalls(level);
        createCallData(level);
    }

    private void cleanCalls(ServerLevel level) {
        var onlinePlayers = level.players().stream().map(Entity::getUUID).toList();
        var calls = Map.copyOf(this.CALLS);
        calls.forEach(((uuid, call) -> {
            if (!onlinePlayers.contains(call.owner.uuid)) this.leaveCall(uuid, call.owner);
            else {
                call.participants.forEach(((uuid1, caller) -> {
                    if (!onlinePlayers.contains(caller.uuid)) this.leaveCall(uuid, caller);
                }));
            }
        }));
    }

    private void createCallData(ServerLevel level) {
        var inflate = 3.5;

        this.CALLS.forEach(((uuid, call) -> {
            var blocks = Map.copyOf(this.BLOCKS.get(uuid) != null ? this.BLOCKS.get(uuid) : new HashMap<>());
            if (!blocks.isEmpty()) {
                blocks.forEach((block, existing) -> {
                    var entities = level.getServer().getLevel(block.level()).getEntitiesOfClass(Player.class, new AABB(block.pos()).move(0, 1, 0).inflate(inflate)).stream().map(Entity::getUUID).toList();
                    var rem = existing.stream().filter(u -> !entities.contains(u)).toList();
                    var add = entities.stream().filter(u -> !existing.contains(u)).toList();
                    rem.forEach(existing::remove);
                    existing.addAll(add);
                    if (!rem.isEmpty() || !add.isEmpty()) {
                        this.BLOCKS.put(uuid, MapUtil.add(block, existing, blocks));
                        this.setDirty();
                    }
                });
            }
        }));
    }

    public record Caller(UUID uuid, @Nullable UUID item, @Nullable WorldPos location) {

        public static Caller from(CompoundTag tag) {
            return new Caller(tag.getUUID("uuid"), NBTUtil.getNullable(tag.getCompound("item"), NBTUtil::getUUID), NBTUtil.getNullable(tag.getCompound("location"), WorldPos::from));
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.putUUID("uuid", this.uuid);
            tag.put("item", NBTUtil.putNullable(this.item, NBTUtil::putUUID));
            tag.put("location", NBTUtil.putNullable(this.location, WorldPos::save));
            return  tag;
        }
    }

    public record Call(UUID id, Caller owner, Map<UUID, Caller> participants) {

        public Call updateParticipants(Consumer<Map<UUID, Caller>> consumer) {
            consumer.accept(participants);
            return new Call(this.id, this.owner, participants);
        }

        public static Call from(CompoundTag tag) {
            return new Call(tag.getUUID("id"), Caller.from(tag.getCompound("owner")), NBTUtil.getMap(tag.getCompound("participants"), NBTUtil::getUUID, Caller::from));
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.putUUID("id", this.id);
            tag.put("owner", this.owner.save());
            tag.put("participants", NBTUtil.putMap(this.participants, NBTUtil::putUUID, Caller::save));
            return  tag;
        }
    }
}
