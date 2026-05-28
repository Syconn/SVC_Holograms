package mod.syconn.svc.blocks;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.blockentity.HoloProjectorBlockEntity;
import mod.syconn.svc.client.ClientHooks;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.utils.block.WorldPos;
import mod.syconn.svc.utils.interfaces.IEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HoloProjectorBlock extends FaceAttachedHorizontalDirectionalBlock implements IEntityBlock {

    private boolean playAudio = true;

    public HoloProjectorBlock() {
        super(Properties.of().noCollission().strength(0.5F));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.WALL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (pState.getValue(FACE) == AttachFace.CEILING)
            return Block.box(3, 15, 3, 13, 16, 13);
        else if (pState.getValue(FACE) == AttachFace.FLOOR)
            return Block.box(3, 0, 3, 13, 1, 13);
        else {
            if (pState.getValue(FACING) == Direction.NORTH)
                return Block.box(3, 3, 15, 13, 13, 16);
            if (pState.getValue(FACING) == Direction.SOUTH)
                return Block.box(3, 3, 0, 13, 13, 1);
            if (pState.getValue(FACING) == Direction.WEST)
                return Block.box(15, 3, 3, 16, 13, 13);
            return Block.box(0, 3, 3, 1, 13, 13);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel) level.getBlockEntity(pos, ModBlockEntities.HOLO_PROJECTOR.get()).ifPresent(b -> HologramNetwork.get(serverLevel).blockRemoved(b.getCallId(), new WorldPos(level.dimension(), pos)));
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new HoloProjectorBlockEntity(pPos, pState);
    }

    @Override
    public @NotNull InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> GameInstance.getClient().setScreen(ClientHooks.createHologramScreen(new WorldPos(pLevel.dimension(), pPos), null)));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return !level.isClientSide ? createTickerHelper(blockEntityType, ModBlockEntities.HOLO_PROJECTOR.get(), HoloProjectorBlockEntity::tick) : null;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            if (level.getBlockEntity(pos) instanceof HoloProjectorBlockEntity blockEntity) {
                if (blockEntity.getCallId() == null) this.playAudio = true;
                else if (this.playAudio) {
                    ClientHooks.playerHoloSound(pos);
                    this.playAudio = false;
                }
            }
        });
    }
}
