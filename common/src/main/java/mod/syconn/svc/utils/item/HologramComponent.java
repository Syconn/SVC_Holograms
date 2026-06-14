package mod.syconn.svc.utils.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.core.ModComponents;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.server.savedData.extra.CallData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

public record HologramComponent(UUID receiverID, @Nullable UUID renderTarget, String soloRender) {

    public static final StreamCodec<RegistryFriendlyByteBuf, HologramComponent> STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, HologramComponent::receiverID, ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
            component -> Optional.ofNullable(component.renderTarget()), ByteBufCodecs.STRING_UTF8, HologramComponent::soloRender,
            (receiverID, renderTarget, soloRender) -> new HologramComponent(receiverID, renderTarget.orElse(null), soloRender));

    public static final Codec<HologramComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(UUIDUtil.CODEC.fieldOf("receiver_id").forGetter(HologramComponent::receiverID),
                    UUIDUtil.CODEC.optionalFieldOf("render_target").forGetter(component -> Optional.ofNullable(component.renderTarget())), Codec.STRING.fieldOf("solo_render")
                    .forGetter(HologramComponent::soloRender)).apply(instance, (receiverID, renderTarget, soloRender) -> new HologramComponent(receiverID, renderTarget.orElse(null), soloRender)));

    public static void serverHandling(Player player, boolean heldItem, ItemStack stack) {
        if (GameInstance.getServer() == null) return;

        update(stack, component -> {
            var renderTarget = component.renderTarget;
            var soloRender = component.soloRender;

            var network = HologramNetwork.get(GameInstance.getServer().overworld());
            var receiver = network.getItemReceiver(component.receiverID);
            if (receiver == null) network.registerItemReceiver(component.receiverID);
            else if (!heldItem && receiver.callID != null && network.getCall(receiver.callID) != null) network.leaveCall(receiver.callID, network.getCall(receiver.callID).callers.get(receiver.userID));
            else if (receiver.callID != null && network.getCall(receiver.callID) != null) {
                boolean legalCall = true, exitLoop = false;
                for (var entries : network.getCall(receiver.callID).renderMembers.entrySet()) {
                    for (var uuid : entries.getValue().keySet()) {
                        if (!uuid.equals(player.getUUID())) {
                            renderTarget = uuid;
                            exitLoop = true;
                            break;
                        }
                    }
                    if (exitLoop) break;
                }
                if (!exitLoop) {
                    for (var entry : network.getCall(receiver.callID).callers.entrySet()) {
                        if (!entry.getKey().equals(player.getUUID()) && entry.getValue().type == CallData.ReceiverType.ITEM) {
                            renderTarget = entry.getKey();
                            exitLoop = true;
                        }
                        if (entry.getValue().type == CallData.ReceiverType.NULL) legalCall = false;
                    }
                }
                if (!legalCall || !exitLoop) renderTarget = null;
                if (legalCall) soloRender = "";
            } else renderTarget = null;

            return new HologramComponent(component.receiverID, renderTarget, soloRender);
        });
    }

    public static HologramComponent getOrCreate(ItemStack stack) {
        return stack.getOrDefault(ModComponents.HOLOGRAM_COMPONENT.get(), create());
    }

    private static HologramComponent create() {
        return new HologramComponent(UUID.randomUUID(), null, "");
    }

    public static void update(ItemStack stack, UnaryOperator<HologramComponent> function) {
        stack.update(ModComponents.HOLOGRAM_COMPONENT.get(), create(), function);
    }
}
