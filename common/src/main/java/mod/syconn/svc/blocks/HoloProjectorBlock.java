package mod.syconn.svc.blocks;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.blockentity.HoloProjectorBlockEntity;
import mod.syconn.svc.client.ClientHooks;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.block.WorldPos;
import mod.syconn.svc.utils.interfaces.IEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
import org.joml.Vector3f;

import java.util.UUID;

public class HoloProjectorBlock extends FaceAttachedHorizontalDirectionalBlock implements IEntityBlock {

//    private boolean playAudio = true; TODO Find Audio Solution

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
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level instanceof ServerLevel sl && level.getBlockEntity(pos) instanceof HoloProjectorBlockEntity be) {
            if (be.getReceiverUUID() == null) be.setReceiverUUID(UUID.randomUUID());
            HologramNetwork.get(sl).registerReceiver(be.getReceiverUUID(), new WorldPos(level.dimension(), pos));
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel sl && level.getBlockEntity(pos) instanceof HoloProjectorBlockEntity be) HologramNetwork.get(sl).unregisterReceiver(be.getReceiverUUID());
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
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> GameInstance.getClient().setScreen(ClientHooks.createHologramScreen(pPos, null)));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.HOLO_PROJECTOR.get(), HoloProjectorBlockEntity::tick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            if (level.getGameTime() % 4 == 0 && level.getBlockEntity(pos) instanceof HoloProjectorBlockEntity be && be.isActive()) {
                double centerX = pos.getX() + 0.5, centerY = pos.getY() + 0.4, centerZ = pos.getZ() + 0.5;
                var blueParticle = new DustColorTransitionOptions(new Vector3f(0.2f, 0.7f, 1.0f), new Vector3f(1.0f, 1.0f, 1.0f), 1.2f);

                for (int i = 0; i < Mth.randomBetweenInclusive(Constants.RANDOM, 1, 8); i++) {
                    double angle = level.random.nextDouble() * (Math.PI * 2.0), radius = 0.25 + level.random.nextDouble() * 0.35;
                    double x = centerX + Math.cos(angle) * radius, y = centerY + (level.random.nextDouble() - 0.5) * 0.25, z = centerZ + Math.sin(angle) * radius;
                    double dx = x - centerX, dy = y - centerY, dz = z - centerZ;
                    var len = Math.sqrt(dx * dx + dy * dy + dz * dz);

                    if (len > 0.0001) {
                        dx /= len;
                        dy /= len;
                        dz /= len;
                    }

                    double tangentX = -dz, tangentZ = dx;
                    var pulse = 0.8f + 0.2f * Mth.sin(level.getGameTime() * 0.1f);
                    double outwardSpeed = 0.012 * pulse, swirlSpeed = 0.004;

                    if (level.random.nextFloat() < 0.45f) level.addParticle(ParticleTypes.END_ROD, x, y, z, dx * 0.04 + tangentX * 0.01, 0.01, dz * 0.04 + tangentZ * 0.01);
                    else level.addParticle(blueParticle, x, y, z, dx * outwardSpeed + tangentX * swirlSpeed, dy * outwardSpeed * 0.3 + 0.003, dz * outwardSpeed + tangentZ * swirlSpeed);
                }
            }

//            if (level.getBlockEntity(pos) instanceof HoloProjectorBlockEntity blockEntity) { TODO SOUNDS
//                if (blockEntity.getCallId() == null) this.playAudio = true;
//                else if (this.playAudio) {
//                    ClientHooks.playerHoloSound(pos);
//                    this.playAudio = false;
//                }
//            }
        });
    }
}
