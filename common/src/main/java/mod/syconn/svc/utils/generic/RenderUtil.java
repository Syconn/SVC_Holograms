package mod.syconn.svc.utils.generic;

import mod.syconn.svc.utils.interfaces.IItemExtensions;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RenderUtil {

    private static int slotMainHand = 0;

    public static boolean handleReequipAnimation(@NotNull ItemStack from, @NotNull ItemStack to, int slot) {
        if (from.getItem() instanceof IItemExtensions ext) {
            boolean fromInvalid = from.isEmpty();
            boolean toInvalid = to.isEmpty();

            if (fromInvalid && toInvalid) return false;
            if (fromInvalid || toInvalid) return true;

            boolean changed = false;
            if (slot != -1) {
                changed = slot != slotMainHand;
                slotMainHand = slot;
            }
            return ext.shouldCauseReequipAnimation(from, to, changed);
        }
        return true;
    }
}
