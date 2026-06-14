package mod.syconn.svc.core;

import dev.architectury.registry.registries.DeferredRegister;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.item.HologramComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

import java.util.function.Supplier;

public class ModComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Constants.MOD, Registries.DATA_COMPONENT_TYPE);

    public static final Supplier<DataComponentType<HologramComponent>> HOLOGRAM_COMPONENT = DATA_COMPONENTS.register("", () -> DataComponentType.<HologramComponent>builder().persistent(HologramComponent.CODEC)
            .networkSynchronized(HologramComponent.STREAM_CODEC).build());
}
