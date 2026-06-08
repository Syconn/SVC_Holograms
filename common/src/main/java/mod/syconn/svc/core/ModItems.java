package mod.syconn.svc.core;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import mod.syconn.svc.item.HoloProjectorItem;
import mod.syconn.svc.utils.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Constants.MOD, Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Constants.MOD, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<Item> HOLO_PROJECTOR = ITEMS.register("holo_projector", () -> new HoloProjectorItem(ModBlocks.HOLO_PROJECTOR.get(), new Item.Properties()));

    public static final RegistrySupplier<CreativeModeTab> TAB = TABS.register("svc", () -> CreativeTabRegistry.create(
            Component.translatable("itemGroup." + Constants.MOD + ".svc"), () -> new ItemStack(HOLO_PROJECTOR.get())));
}