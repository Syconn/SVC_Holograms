package mod.syconn.svc.server.savedData.extra;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.compat.vc.VoiceChatManager;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.network.Network;
import mod.syconn.svc.network.packets.client.MessagePlayerPacket;
import mod.syconn.svc.utils.block.WorldPos;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CallData {

    public enum ReceiverType { BLOCK, ITEM, NULL }

    public static class BlockReceiver {
        public UUID blockID;
        public WorldPos pos;
        public @Nullable UUID callID;

        public BlockReceiver(UUID blockID, WorldPos pos, @Nullable UUID callID) {
            this.blockID = blockID;
            this.pos = pos;
            this.callID = callID;
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.put("blockID", NBTUtil.putUUID(this.blockID));
            tag.put("pos", this.pos.save());
            tag.put("callID", NBTUtil.putNullable(this.callID, NBTUtil::putUUID));
            return tag;
        }

        public static BlockReceiver from(CompoundTag tag) {
            return new BlockReceiver(NBTUtil.getUUID(tag.getCompound("blockID")), WorldPos.from(tag.getCompound("pos")), NBTUtil.getNullable(tag.getCompound("callID"), NBTUtil::getUUID));
        }
    }

    public static class ItemReceiver {
        public UUID itemID;
        public @Nullable UUID userID;
        public @Nullable UUID callID;

        public ItemReceiver(UUID itemID, @Nullable UUID userID, @Nullable UUID callID) {
            this.itemID = itemID;
            this.userID = userID;
            this.callID = callID;
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.put("itemID", NBTUtil.putUUID(this.itemID));
            tag.put("userID", NBTUtil.putNullable(this.userID, NBTUtil::putUUID));
            tag.put("callID", NBTUtil.putNullable(this.callID, NBTUtil::putUUID));
            return tag;
        }

        public static ItemReceiver from(CompoundTag tag) {
            return new ItemReceiver(NBTUtil.getUUID(tag.getCompound("itemID")), NBTUtil.getNullable(tag.getCompound("userID"), NBTUtil::getUUID), NBTUtil.getNullable(tag.getCompound("callID"), NBTUtil::getUUID));
        }
    }

    public static class Callee {
        public UUID playerUUID;
        public boolean owner;
        public ReceiverType type;
        public @Nullable UUID receiverID;

        public Callee(UUID playerUUID, boolean owner, ReceiverType type, @Nullable UUID receiverID) {
            this.playerUUID = playerUUID;
            this.owner = owner;
            this.type = type;
            this.receiverID = receiverID;
        }

        public Callee(UUID playerUUID) {
            this.playerUUID = playerUUID;
            this.owner = false;
            this.type = ReceiverType.NULL;
            this.receiverID = null;
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.put("playerUUID", NBTUtil.putUUID(this.playerUUID));
            tag.putBoolean("owner", this.owner);
            tag.put("type", NBTUtil.putEnum(type));
            tag.put("receiverID", NBTUtil.putNullable(this.receiverID, NBTUtil::putUUID));
            return tag;
        }

        public static Callee from(CompoundTag tag) {
            return new Callee(NBTUtil.getUUID(tag.getCompound("playerUUID")), tag.getBoolean("owner"), NBTUtil.getEnum(ReceiverType.class, tag.getCompound("type")), NBTUtil.getNullable(tag.getCompound("receiverID"), NBTUtil::getUUID));
        }
    }

    public static class Call {
        public Map<UUID, Map<UUID, Vec3>> renderMembers;
        public UUID callID;
        public UUID owner;
        public boolean secure;
        public Map<UUID, Callee> callers;

        public Call(UUID callID, UUID owner, boolean secure, Map<UUID, Callee> callers) {
            this.callID = callID;
            this.owner = owner;
            this.secure = secure;
            this.callers = callers;
            this.renderMembers = new HashMap<>();
        }

        public Call(UUID callID, UUID owner, boolean secure, Map<UUID, Callee> callers, Map<UUID, Map<UUID, Vec3>> renderMembers) {
            this.renderMembers = renderMembers;
            this.callID = callID;
            this.owner = owner;
            this.secure = secure;
            this.callers = callers;
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.put("callID", NBTUtil.putUUID(this.callID));
            tag.put("owner", NBTUtil.putUUID(this.owner));
            tag.putBoolean("secure", this.secure);
            tag.put("callers", NBTUtil.putMap(this.callers, NBTUtil::putUUID, Callee::save));
            tag.put("renderMembers", NBTUtil.putMap(this.renderMembers, NBTUtil::putUUID, uuid -> NBTUtil.putMap(uuid, NBTUtil::putUUID, NBTUtil::putVec3)));
            return tag;
        }

        public static Call from(CompoundTag tag) {
            return new Call(NBTUtil.getUUID(tag.getCompound("callID")), NBTUtil.getUUID(tag.getCompound("owner")), tag.getBoolean("secure"), NBTUtil.getMap(tag.getCompound("callers"), NBTUtil::getUUID, Callee::from),
                    NBTUtil.getMap(tag.getCompound("renderMembers"), NBTUtil::getUUID, t -> NBTUtil.getMap(t, NBTUtil::getUUID, NBTUtil::getVec3)));
        }
    }

    public static class CallManager {
        private final Map<UUID, Call> CALLS = new HashMap<>();
        private final Map<UUID, BlockReceiver> BLOCK_RECEIVERS = new HashMap<>();
        private final Map<UUID, ItemReceiver> ITEM_RECEIVERS = new HashMap<>();
        private VoiceChatManager vc = new VoiceChatManager();
        private final Set<UUID> RENDER_CACHE = new HashSet<>();
        private boolean dirty = true;

        public void createCall(List<Callee> members, boolean secure) {
            if (members.size() <= 1) return;
            var owner = members.stream().filter(v -> v.owner).findFirst().orElse(null);
            if (owner == null) return;

            Map<UUID, Callee> map = new HashMap<>();
            for (Callee c : members) {
                map.put(c.playerUUID, c);
                if (!c.owner) notifyCallInvite(owner.playerUUID, c.playerUUID);
            }

            var callId = UUID.randomUUID();
            var call = new Call(callId, owner.playerUUID, secure, map);
            this.CALLS.put(callId, call);
            if (owner.type == ReceiverType.BLOCK) {
                this.BLOCK_RECEIVERS.computeIfPresent(owner.receiverID, (id, rec) -> {
                    rec.callID = callId;
                    return rec;
                });
            } else if (owner.type == ReceiverType.ITEM) {
                this.ITEM_RECEIVERS.computeIfPresent(owner.receiverID, (id, rec) -> {
                    rec.callID = callId;
                    rec.userID = owner.playerUUID;
                    return rec;
                });
            }
            this.vc.createCall(callId);
            this.vc.joinCall(callId, owner.playerUUID);
        }

        public void connectToCall(UUID callID, Callee callee) {
            var call = this.CALLS.get(callID);
            if (call == null || callee.type == ReceiverType.NULL) return;
            if (call.secure && !call.callers.containsKey(callee.playerUUID)) return;

            for (var caller : call.callers.entrySet()) if (caller.getValue().type != ReceiverType.NULL) notifyJoinedCall(caller.getKey(), callee.playerUUID);

            call.callers.put(callee.playerUUID, callee);

            if (callee.type == ReceiverType.BLOCK) {
                this.BLOCK_RECEIVERS.computeIfPresent(callee.receiverID, (id, rec) -> {
                    rec.callID = callID;
                    return rec;
                });
            } else if (callee.type == ReceiverType.ITEM) {
                this.dirty = true;
                this.ITEM_RECEIVERS.computeIfPresent(callee.receiverID, (id, rec) -> {
                    rec.callID = callID;
                    rec.userID = callee.playerUUID;
                    return rec;
                });
            }
            this.vc.joinCall(callID, callee.playerUUID);
        }

        public void leaveCall(UUID callId, Callee callee) {
            var call = this.CALLS.get(callId);

            if (call == null) return;
            var removed = call.callers.remove(callee.playerUUID);

            for (var caller : call.callers.entrySet()) if (caller.getValue().type != ReceiverType.NULL) notifyLeftCall(caller.getKey(), callee.playerUUID);

            if (removed == null) return;
            if (removed.type == ReceiverType.BLOCK && removed.receiverID != null) {
                var receiver = this.BLOCK_RECEIVERS.get(removed.receiverID);
                if (receiver != null && receiver.callID != null && receiver.callID.equals(callId)) receiver.callID = null;
            } else if (removed.type == ReceiverType.ITEM && removed.receiverID != null) {
                var receiver = this.ITEM_RECEIVERS.get(removed.receiverID);
                if (receiver != null && receiver.callID != null && receiver.callID.equals(callId)) {
                    receiver.callID = null;
                    receiver.userID = null;
                }
            }

            if (call.callers.size() <= 1) {
                this.CALLS.remove(callId);
                this.vc.endCall(callId);
                this.validateReceivers();
            }

            this.dirty = true;
        }

        public void playerLeftServer(UUID playerId) {
            var iterator = CALLS.entrySet().iterator();

            while (iterator.hasNext()) {
                var entry = iterator.next();
                var call = entry.getValue();
                var removed = call.callers.remove(playerId);

                if (removed == null) continue;
                if (removed.type == ReceiverType.BLOCK && removed.receiverID != null) {
                    var receiver = BLOCK_RECEIVERS.get(removed.receiverID);
                    if (receiver != null && receiver.callID != null && receiver.callID.equals(call.callID)) receiver.callID = null;
                } else if (removed.type == ReceiverType.ITEM && removed.receiverID != null) {
                    var receiver = ITEM_RECEIVERS.get(removed.receiverID);
                    if (receiver != null && receiver.callID != null && receiver.callID.equals(call.callID)) receiver.callID = null;
                }
                if (call.callers.isEmpty()) iterator.remove();
            }

            this.dirty = true;
        }

        public void setRenderMembers(UUID callID, UUID receiverID, Map<UUID, Vec3> renderMembers) {
            var call = this.CALLS.get(callID);
            if (call == null) return;

            var previous = call.renderMembers.get(receiverID);
            if (!renderMembers.equals(previous)) {
                call.renderMembers.put(receiverID, new HashMap<>(renderMembers));
                this.dirty = true;
            }

            for (var member : renderMembers.keySet()) this.vc.joinCall(callID, member);
        }

        public void registerBlockReceiver(UUID blockID, WorldPos pos) {
            this.BLOCK_RECEIVERS.put(blockID, new BlockReceiver(blockID, pos, null));
        }

        public void registerItemReceiver(UUID itemID) {
            this.ITEM_RECEIVERS.put(itemID, new ItemReceiver(itemID, null, null));
        }

        public void unregisterBlockReceiver(UUID blockID) {
            var rec = this.BLOCK_RECEIVERS.remove(blockID);
            if (rec != null) removeCallReceiver(rec);
        }

        public void removeCallReceiver(BlockReceiver receiver) {
            if (receiver.callID == null) return;

            var call = this.CALLS.get(receiver.callID);
            call.callers.entrySet().removeIf(entry -> receiver.blockID.equals(entry.getValue().receiverID));
        }

        public BlockReceiver getBlockReceiver(UUID receiverID) {
            return this.BLOCK_RECEIVERS.get(receiverID);
        }

        public ItemReceiver getItemReceiver(UUID receiverID) {
            return this.ITEM_RECEIVERS.get(receiverID);
        }

        public Call getCall(UUID callID) {
            return this.CALLS.get(callID);
        }

        public List<Call> getCallsForPlayer(UUID playerID) {
            return this.CALLS.values().stream().filter(call -> !call.secure || call.callers.containsKey(playerID)).toList();
        }

        public List<BlockReceiver> getDebugData() {
            return BLOCK_RECEIVERS.values().stream().toList();
        }

        public Set<UUID> getRenderCache() {
            if (dirty) rebuildCache();
            return RENDER_CACHE;
        }

        public void rebuildCache() {
            RENDER_CACHE.clear();
            for (var call : CALLS.values()) {
                for (var players : call.renderMembers.values()) {
                    for (var playerID : players.keySet()) {
                        if (playerID != null) RENDER_CACHE.add(playerID);
                    }
                }
                for (var entry : call.callers.entrySet()) {
                    if (entry.getValue().type == CallData.ReceiverType.ITEM) {
                        if (entry.getKey() != null) RENDER_CACHE.add(entry.getKey());
                    }
                }
            }
            dirty = false;
        }

        public void validateCallLog() {
            var server = GameInstance.getServer();
            if (server == null) return;

            var players = server.getPlayerList();
            this.CALLS.entrySet().removeIf(entry -> {
                var call = entry.getValue();
                call.callers.values().removeIf(v -> players.getPlayer(v.playerUUID) == null);
                if (call.callers.size() <= 1) this.vc.endCall(entry.getKey());
                return call.callers.size() <= 1;
            });
        }

        public void validateReceivers() {
            var server = GameInstance.getServer();
            if (server == null) return;

            this.BLOCK_RECEIVERS.entrySet().removeIf(entry -> {
                var level = server.getLevel(entry.getValue().pos.level());
                if (level == null) return true;
                return level.getBlockEntity(entry.getValue().pos.pos(), ModBlockEntities.HOLO_PROJECTOR.get()).isEmpty();
            });
            this.BLOCK_RECEIVERS.forEach((uuid, receiver) -> { if (receiver.callID != null && !this.CALLS.containsKey(receiver.callID)) receiver.callID = null; });
        }

        public void tick() {
            this.vc.tick(this);
        }

        private void notifyCallInvite(UUID ownerID, UUID targetID) {
            if (GameInstance.getServer() != null) {
                var owner = GameInstance.getServer().getPlayerList().getPlayer(ownerID);
                var serverPlayer = GameInstance.getServer().getPlayerList().getPlayer(targetID);
                if (serverPlayer != null && owner != null) Network.CHANNEL.sendToPlayer(serverPlayer, new MessagePlayerPacket(Component.literal("Incoming HoloCommunication from " + owner.getName().getString()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
            }
        }

        private void notifyJoinedCall(UUID ownerID, UUID joinerID) {
            if (GameInstance.getServer() != null) {
                var owner = GameInstance.getServer().getPlayerList().getPlayer(ownerID);
                var joinedPlayer = GameInstance.getServer().getPlayerList().getPlayer(joinerID);
                if (joinedPlayer != null && owner != null) Network.CHANNEL.sendToPlayer(owner, new MessagePlayerPacket(Component.literal(joinedPlayer.getName().getString() + " has joined the HoloCommunication").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
            }
        }

        private void notifyLeftCall(UUID ownerID, UUID leftID) {
            if (GameInstance.getServer() != null) {
                var owner = GameInstance.getServer().getPlayerList().getPlayer(ownerID);
                var leftPlayer = GameInstance.getServer().getPlayerList().getPlayer(leftID);
                if (leftPlayer != null && owner != null) Network.CHANNEL.sendToPlayer(owner, new MessagePlayerPacket(Component.literal(leftPlayer.getName().getString() + " has left the HoloCommunication").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
            }
        }

        public @NotNull CompoundTag save() {
            var tag = new CompoundTag();
            tag.put("calls", NBTUtil.putMap(this.CALLS, NBTUtil::putUUID, Call::save));
            tag.put("block_receivers", NBTUtil.putMap(this.BLOCK_RECEIVERS, NBTUtil::putUUID, BlockReceiver::save));
            tag.put("item_receivers", NBTUtil.putMap(this.ITEM_RECEIVERS, NBTUtil::putUUID, ItemReceiver::save));
            tag.put("vc", this.vc.save());
            tag.put("cache", NBTUtil.putSet(this.RENDER_CACHE, NBTUtil::putUUID));
            tag.putBoolean("dirty", this.dirty);
            return tag;
        }

        public void read(CompoundTag tag) {
            this.CALLS.clear();
            this.BLOCK_RECEIVERS.clear();
            this.ITEM_RECEIVERS.clear();
            this.RENDER_CACHE.clear();
            this.CALLS.putAll(NBTUtil.getMap(tag.getCompound("calls"), NBTUtil::getUUID, Call::from));
            this.BLOCK_RECEIVERS.putAll(NBTUtil.getMap(tag.getCompound("block_receivers"), NBTUtil::getUUID, BlockReceiver::from));
            this.ITEM_RECEIVERS.putAll(NBTUtil.getMap(tag.getCompound("item_receivers"), NBTUtil::getUUID, ItemReceiver::from));
            this.vc = VoiceChatManager.load(tag.getCompound("vc"));
            this.RENDER_CACHE.addAll(NBTUtil.getSet(tag.getCompound("cache"), NBTUtil::getUUID));
            this.dirty = tag.getBoolean("dirty");
            this.validateCallLog();
            this.validateReceivers();
        }
    }
}
