package mod.syconn.svc.utils.entity.extra;

import mod.syconn.svc.mixin.EntityAccess;
import mod.syconn.svc.mixin.LivingEntityAccess;
import mod.syconn.svc.utils.interfaces.IExtraRenderInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LivingEntityExtraRenderInfo implements IExtraRenderInfo { // TODO NO SHIFT ANIMIATION, OR WALK

    @NotNull
    private ItemStack mainHand = ItemStack.EMPTY;

    @NotNull
    private ItemStack offHand = ItemStack.EMPTY;

    @NotNull
    private Pose pose = Pose.STANDING;

    private float yBodyRot;
    private float yHeadRot;
    private boolean swinging;
    private int swingTime;
    private InteractionHand swingingArm;
    private boolean fallFlying;
    private boolean isShiftKeyDown;

    @Override
    public void tickFakeEntity(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity living)) return;
        if (fallFlying) ((LivingEntityAccess)living).setFallFlyTicks(living.getFallFlyingTicks() + 1);
        else ((LivingEntityAccess)living).setFallFlyTicks(0);
        living.calculateEntityAnimation(false);
    }

    @Override
    public void updateFakeEntity(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity living)) return;
        living.setItemInHand(InteractionHand.MAIN_HAND, this.mainHand);
        living.setItemInHand(InteractionHand.OFF_HAND, this.offHand);
        living.yBodyRot = this.yBodyRot;
        living.yBodyRotO = this.yBodyRot;
        living.yHeadRot = this.yHeadRot;
        living.yHeadRotO = this.yHeadRot;
        living.swinging = this.swinging;
//        living.swingTime = this.swingTime;
        living.swingingArm = this.swingingArm;
        ((EntityAccess)entity).invokeSetSharedFlag(1, this.isShiftKeyDown);
        ((EntityAccess)entity).invokeSetSharedFlag(7, this.fallFlying);
        living.setPose(this.pose);
    }

    @Override
    public void setupEntityOnCreate(@NotNull Entity entity) { }

    @Override
    public void getInfoServerSide(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity living)) return;
        this.mainHand = living.getMainHandItem();
        this.offHand = living.getOffhandItem();
        this.fallFlying = living.isFallFlying();
        this.pose = living.getPose();
        this.yBodyRot = living.yBodyRot;
        this.yHeadRot = living.yHeadRot;
        this.swinging = living.swinging;
//        this.swingTime = living.swingTime;
        this.swingingArm = living.swingingArm;
        this.isShiftKeyDown = living.isShiftKeyDown();
    }

    @Override
    public void getInfoClientSide(FriendlyByteBuf buffer) {
        this.mainHand = buffer.readItem();
        this.offHand = buffer.readItem();
        this.fallFlying = buffer.readBoolean();
        this.pose = buffer.readEnum(Pose.class);
        this.yBodyRot = buffer.readFloat();
        this.yHeadRot = buffer.readFloat();

        boolean wasSwinging = this.swinging;
        this.swinging = buffer.readBoolean();
        if (this.swinging && !wasSwinging) {
            this.swingTime = 0;
        }

//        this.swingTime = buffer.readInt();
        this.swingingArm = buffer.readEnum(InteractionHand.class);
        this.isShiftKeyDown = buffer.readBoolean();
    }

    @Override
    public void encodeInfoServerSide(FriendlyByteBuf buffer) {
        buffer.writeItem(this.mainHand);
        buffer.writeItem(this.offHand);
        buffer.writeBoolean(this.fallFlying);
        buffer.writeEnum(this.pose);
        buffer.writeFloat(this.yBodyRot);
        buffer.writeFloat(this.yHeadRot);
        buffer.writeBoolean(this.swinging);
//        buffer.writeInt(this.swingTime);
        buffer.writeEnum(this.swingingArm);
        buffer.writeBoolean(this.isShiftKeyDown);
    }
}
