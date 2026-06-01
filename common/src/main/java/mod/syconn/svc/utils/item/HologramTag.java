package mod.syconn.svc.utils.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

public class HologramTag {

    private static final String ID = "hologramData";
    private final UUID receiverID;
    private String soloRender = "";

    public HologramTag() {
        this.receiverID = UUID.randomUUID();
    }

    public HologramTag(CompoundTag tag) {
        this.receiverID = tag.hasUUID("receiverID") ? tag.getUUID("receiverID") : UUID.randomUUID();
        this.soloRender = tag.getString("soloRender");
    }

    public void setSoloRender(String soloRender) {
        this.soloRender = soloRender;
    }

    public UUID getReceiverID() {
        return receiverID;
    }

    public String getSoloRender() {
        return soloRender;
    }

    private CompoundTag save() {
        var tag = new CompoundTag();
        tag.putUUID("receiverID", this.receiverID);
        tag.putString("soloRender", this.soloRender);
        return tag;
    }

    public static HologramTag getOrCreate(ItemStack stack) {
        var tag = !stack.getOrCreateTag().contains(ID) ? create() : new HologramTag(stack.getOrCreateTag().getCompound(ID));
        tag.change(stack);
        return tag;
    }

    public static ItemStack update(ItemStack stack, Consumer<HologramTag> consumer) {
        var tag = getOrCreate(stack);
        consumer.accept(tag);
        return tag.change(stack);
    }

    private static HologramTag create() {
        return new HologramTag();
    }

    public ItemStack change(ItemStack stack) {
        stack.getOrCreateTag().put(ID, save());
        return stack;
    }
}
