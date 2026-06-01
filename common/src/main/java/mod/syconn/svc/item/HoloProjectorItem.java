package mod.syconn.svc.item;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.blocks.HoloProjectorBlock;
import mod.syconn.svc.client.ClientHooks;
import mod.syconn.svc.core.ModItems;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.utils.client.HologramData;
import mod.syconn.svc.utils.interfaces.IItemExtensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class HoloProjectorItem extends BlockItem implements IItemExtensions {

    public HoloProjectorItem(HoloProjectorBlock block, Properties properties) {
        super(block, properties.stacksTo(1).arch$tab(ModItems.TAB));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> GameInstance.getClient().setScreen(ClientHooks.createHologramScreen(null, stack)));
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack from, @NotNull ItemStack to, boolean changed) {
        return changed;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
//        var id = HologramData.HologramTag.getOrCreate(stack);
//        if (level instanceof ServerLevel serverLevel) { TODO Deal with item handling
//            var network = HologramNetwork.get(serverLevel);
//            var callId = network.getCallId(id.itemId);
//            if (callId != null && network.getCall(callId) != null && network.getBlockData(callId) != null) {
//                var call = network.getCall(callId);
//                var uuids = network.getBlockData(callId).values().stream().flatMap(Collection::stream).toList();
//                if (!uuids.isEmpty()) {
//                    var uuid = !call.owner().uuid().equals(entity.getUUID()) && uuids.contains(call.owner().uuid()) ? call.owner().uuid() : uuids.get(0);
//                    if (serverLevel.getPlayerByUUID(uuid) == null && id.uuid != null) HologramData.HologramTag.update(stack, null);
//                    else if (!uuid.equals(id.uuid)) HologramData.HologramTag.update(stack, uuid);
//                    return;
//                }
//            }
//            if (id.uuid != null) HologramData.HologramTag.update(stack, null);
//        }
    }
}
