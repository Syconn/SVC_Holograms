package mod.syconn.svc.utils.interfaces;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IItemExtensions {

    default boolean shouldCauseReequipAnimation(@NotNull ItemStack from, @NotNull ItemStack to, boolean changed) {
        return true;
    }

    default Multimap<Attribute, AttributeModifier> getAttributeModifications(ItemStack stack, EquipmentSlot slot) {
        return ((Item) this).getDefaultAttributeModifiers(slot);
    }

    default boolean onItemSelected(Player player, ItemStack stack) {
        return false;
    }

    default boolean onItemDeselected(Player player, ItemStack stack) {
        return false;
    }
}
