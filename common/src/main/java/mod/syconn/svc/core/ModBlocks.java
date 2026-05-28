package mod.syconn.svc.core;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import mod.syconn.svc.blocks.HoloProjectorBlock;
import mod.syconn.svc.utils.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Constants.MOD, Registries.BLOCK);

    public static final RegistrySupplier<HoloProjectorBlock> HOLO_PROJECTOR = register("holo_projector", HoloProjectorBlock::new);

    private static <B extends Block, I extends BlockItem> RegistrySupplier<B> register(String id, Supplier<B> blockSupplier) {
        return BLOCKS.register(id, blockSupplier);
    }
}
