package mod.syconn.svc.server.savedData.extra;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.network.Network;
import mod.syconn.svc.network.packets.client.MessagePlayerPacket;
import mod.syconn.svc.utils.block.WorldPos;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

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
            return new BlockReceiver(tag.getUUID("blockID"), WorldPos.from(tag.getCompound("pos")), NBTUtil.getNullable(tag.getCompound("callID"), NBTUtil::getUUID));
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
            return new Callee(tag.getUUID("playerUUID"), tag.getBoolean("owner"), NBTUtil.getEnum(ReceiverType.class, tag.getCompound("type")), NBTUtil.getNullable(tag.getCompound("receiverID"), NBTUtil::getUUID));
        }
    }

    public static class Call { // callers: <playerUUID, Callee>
        public UUID callID;
        public UUID owner;
        public boolean secure;
        public Map<UUID, Callee> callers;

        public Call(UUID callID, UUID owner, boolean secure, Map<UUID, Callee> callers) {
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
            return tag;
        }

        public static Call from(CompoundTag tag) {
            return new Call(tag.getUUID("callID"), tag.getUUID("owner"), tag.getBoolean("secure"), NBTUtil.getMap(tag.getCompound("callers"), NBTUtil::getUUID, Callee::from));
        }
    }

    public static class CallManager {

        private final Map<UUID, Call> CALLS = new HashMap<>();
        private final Map<UUID, BlockReceiver> BLOCK_RECEIVERS = new HashMap<>();

        public void createCall(List<Callee> members, boolean secure) { // Returns CallId
            if (members.size() <= 1) return;

            var owner = members.stream().filter(v -> v.owner).findFirst().orElse(null);
            if (owner == null) { // TODO REMOVE LATER
                System.out.println("Call Creation: Missing owner");
                owner = members.get(0);
                owner.owner = true;
            }

            Map<UUID, Callee> map = new HashMap<>();
            for (Callee c : members) {
                map.put(c.playerUUID, c);
                if (!c.owner) notifyPlayers(c.playerUUID);
            }

            var callId = UUID.randomUUID();
            var call = new Call(callId, owner.playerUUID, secure, map);
            this.CALLS.put(callId, call);
            if (owner.type == ReceiverType.BLOCK) {
                this.BLOCK_RECEIVERS.computeIfPresent(owner.receiverID, (id, rec) -> {
                    rec.callID = callId;
                    return rec;
                });
            }
        }

        public void connectToCall(UUID callID, Callee callee) {
            var call = this.CALLS.get(callID);
            if (call == null || callee.type == ReceiverType.NULL) return;
            if (call.secure && !call.callers.containsKey(callID)) return;

            call.callers.put(callee.playerUUID, callee);
            if (callee.type == ReceiverType.BLOCK) {
                this.BLOCK_RECEIVERS.computeIfPresent(callee.receiverID, (id, rec) -> {
                    rec.callID = callID;
                    return rec;
                });
            }
        }

        public void leaveCall(UUID callId, CallData.Callee callee) {
            var call = this.CALLS.get(callId);

            if (call == null) return;
            var removed = call.callers.remove(callee.playerUUID);

            if (removed == null) return;
            if (removed.type == ReceiverType.BLOCK && removed.receiverID != null) {
                var receiver = this.BLOCK_RECEIVERS.get(removed.receiverID);
                if (receiver != null && receiver.callID != null && receiver.callID.equals(callId)) receiver.callID = null;
            }

            if (call.callers.isEmpty()) this.CALLS.remove(callId);
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
                }
                if (call.callers.isEmpty()) iterator.remove();
            }
        }

        public void registerReceiver(UUID blockID, WorldPos pos) {
            this.BLOCK_RECEIVERS.put(blockID, new BlockReceiver(blockID, pos, null));
        }

        public void unregisterReceiver(UUID blockID) {
            var rec = this.BLOCK_RECEIVERS.remove(blockID);
            removeCallReceiver(rec);
        }

        public void removeCallReceiver(BlockReceiver receiver) {
            if (receiver.callID == null) return;

            var call = this.CALLS.get(receiver.callID);
            call.callers.entrySet().removeIf(entry -> receiver.blockID.equals(entry.getValue().receiverID));
        }

        public CallData.BlockReceiver getCallForBlock(UUID receiverID) {
            return this.BLOCK_RECEIVERS.get(receiverID);
        }

        public List<CallData.Call> getCallsForPlayer(UUID playerID) {
            return this.CALLS.values().stream().filter(call -> !call.secure || call.callers.containsKey(playerID)).toList();
        }

        public Map<UUID, BlockPos> getDebugData() {
            return BLOCK_RECEIVERS.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().pos.pos()));
        }

        private void notifyPlayers(UUID callID) {
            var call = CALLS.get(callID);

            for (var callee : call.callers.values()) {
                if (GameInstance.getServer() != null) {
                    var owner = GameInstance.getServer().getPlayerList().getPlayer(call.owner);
                    var serverPlayer = GameInstance.getServer().getPlayerList().getPlayer(callee.playerUUID);
                    if (serverPlayer != null && owner != null) Network.CHANNEL.sendToPlayer(serverPlayer, new MessagePlayerPacket(Component.literal("Incoming HoloCommunication from " + owner.getName().getString()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
                }
            }
        }

        public @NotNull CompoundTag save() {
            var tag = new CompoundTag();
            tag.put("calls", NBTUtil.putMap(this.CALLS, NBTUtil::putUUID, Call::save));
            tag.put("block_receivers", NBTUtil.putMap(this.BLOCK_RECEIVERS, NBTUtil::putUUID, BlockReceiver::save));
            return tag;
        }

        public void read(CompoundTag tag) {
            this.CALLS.clear();
            this.BLOCK_RECEIVERS.clear();
            this.CALLS.putAll(NBTUtil.getMap(tag.getCompound("calls"), NBTUtil::getUUID, Call::from));
            this.BLOCK_RECEIVERS.putAll(NBTUtil.getMap(tag.getCompound("block_receivers"), NBTUtil::getUUID, BlockReceiver::from));
        }
    }
}
