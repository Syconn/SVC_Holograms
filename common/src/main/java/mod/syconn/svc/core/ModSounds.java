package mod.syconn.svc.core;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import mod.syconn.svc.utils.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Constants.MOD, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> HOLOGRAM_ACTIVATE = register("block.hologram.activate");
    public static final RegistrySupplier<SoundEvent> HOLOGRAM_DEACTIVATE = register("block.hologram.deactivate");
    public static final RegistrySupplier<SoundEvent> HOLOGRAM_STATIC = register("block.hologram.static");
    public static final RegistrySupplier<SoundEvent> HOLOGRAM_BUTTON1 = register("block.hologram.button1");
    public static final RegistrySupplier<SoundEvent> HOLOGRAM_BUTTON2 = register("block.hologram.button2");
    public static final RegistrySupplier<SoundEvent> HOLOGRAM_BUTTON3 = register("block.hologram.button3");

    private static RegistrySupplier<SoundEvent> register(String key) {
        return SOUNDS.register(key, () -> SoundEvent.createVariableRangeEvent(Constants.withId(key)));
    }
}
