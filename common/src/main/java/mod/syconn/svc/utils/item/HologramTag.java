package mod.syconn.svc.utils.item;

import dev.architectury.utils.GameInstance;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.generic.NBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.core.appender.rolling.action.IfAll;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class HologramTag {

    private static final String ID = "hologramData";
    private final UUID receiverID;
    private @Nullable UUID renderTarget;
    private String soloRender = "";

    public HologramTag() {
        this.receiverID = UUID.randomUUID();
    }

    public HologramTag(CompoundTag tag) {
        this.receiverID = tag.hasUUID("receiverID") ? tag.getUUID("receiverID") : UUID.randomUUID();
        this.renderTarget = NBTUtil.getNullable(tag.getCompound("renderTarget"), NBTUtil::getUUID);
        this.soloRender = tag.getString("soloRender");
    }

    public void setSoloRender(String soloRender) {
        this.soloRender = soloRender;
    }

    public UUID getReceiverID() {
        return receiverID;
    }

    public @Nullable UUID getRenderTarget() {
        return renderTarget;
    }

    public String getSoloRender() {
        return soloRender;
    }

    public void serverHandling(Player player, boolean heldItem) {
        if (GameInstance.getServer() == null) return;

        var network = HologramNetwork.get(GameInstance.getServer().overworld());
        var receiver = network.getItemReceiver(this.receiverID);
        if (receiver == null) network.registerItemReceiver(this.receiverID);
        else if (!heldItem && receiver.callID != null && network.getCall(receiver.callID) != null) network.leaveCall(receiver.callID, network.getCall(receiver.callID).callers.get(receiver.userID));
        else if (receiver.callID != null && network.getCall(receiver.callID) != null) {
            boolean legalCall = true, exitLoop = false;
            for (var entries : network.getCall(receiver.callID).renderMembers.entrySet()) {
                for (var uuid : entries.getValue().keySet()) {
                    if (!uuid.equals(player.getUUID())) {
                        this.renderTarget = uuid;
                        exitLoop = true;
                        break;
                    }
                }
                if (exitLoop) break;
            }
            if (!exitLoop) {
                for (var entry : network.getCall(receiver.callID).callers.entrySet()) {
                    if (!entry.getKey().equals(player.getUUID()) && entry.getValue().type == CallData.ReceiverType.ITEM) {
                        this.renderTarget = entry.getKey();
                        exitLoop = true;
                    }
                    if (entry.getValue().type == CallData.ReceiverType.NULL) legalCall = false;
                }
            }
            if (!legalCall || !exitLoop) this.renderTarget = null;
            if (legalCall) this.soloRender = "";
        } else this.renderTarget = null;
    }

    private CompoundTag save() {
        var tag = new CompoundTag();
        tag.putUUID("receiverID", this.receiverID);
        tag.put("renderTarget", NBTUtil.putNullable(this.renderTarget, NBTUtil::putUUID));
        tag.putString("soloRender", this.soloRender);
        return tag;
    }

    public static HologramTag getOrCreate(ItemStack stack) {
        var tag = !stack.getOrCreateTag().contains(ID) ? create() : new HologramTag(stack.getOrCreateTag().getCompound(ID));
        if (!stack.getOrCreateTag().contains(ID)) tag.change(stack);
        return tag;
    }

    public static void update(ItemStack stack, Consumer<HologramTag> consumer) {
        var tag = getOrCreate(stack);
        consumer.accept(tag);
        tag.change(stack);
    }

    private static HologramTag create() {
        return new HologramTag();
    }

    public void change(ItemStack stack) {
        stack.getOrCreateTag().put(ID, save());
    }
}
