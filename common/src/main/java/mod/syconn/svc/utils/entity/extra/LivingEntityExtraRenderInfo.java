package mod.syconn.svc.utils.entity.extra;

import mod.syconn.svc.mixin.EntityAccess;
import mod.syconn.svc.mixin.LivingEntityAccess;
import mod.syconn.svc.utils.entity.HologramPlayer;
import mod.syconn.svc.utils.interfaces.IExtraRenderInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LivingEntityExtraRenderInfo implements IExtraRenderInfo {

    @NotNull
    private ItemStack mainHand = ItemStack.EMPTY;

    @NotNull
    private ItemStack offHand = ItemStack.EMPTY;

    @NotNull
    private Pose pose = Pose.STANDING;

    private float yBodyRot;
    private float yHeadRot;
    private boolean swinging;
    private InteractionHand swingingArm;
    private boolean fallFlying;
    private boolean isShiftKeyDown;
    private boolean usingItem;
    private InteractionHand usingHand;

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
        if (living instanceof HologramPlayer hologram) hologram.handleNetworkSwing(this.swinging, this.swingingArm);
        ((EntityAccess)entity).invokeSetSharedFlag(1, this.isShiftKeyDown);
        ((EntityAccess)entity).invokeSetSharedFlag(7, this.fallFlying);
        living.setPose(this.pose);
        if (this.usingItem && !living.isUsingItem()) living.startUsingItem(this.usingHand);
        else if (!this.usingItem) living.stopUsingItem();
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
        this.swingingArm = living.swingingArm;
        this.isShiftKeyDown = living.isShiftKeyDown();
        this.usingItem = living.isUsingItem();
        this.usingHand = living.getUsedItemHand();
    }

    @Override
    public void getInfoClientSide(RegistryFriendlyByteBuf buffer) {
        this.mainHand = ItemStack.STREAM_CODEC.decode(buffer);
        this.offHand = ItemStack.STREAM_CODEC.decode(buffer);
        this.fallFlying = buffer.readBoolean();
        this.pose = buffer.readEnum(Pose.class);
        this.yBodyRot = buffer.readFloat();
        this.yHeadRot = buffer.readFloat();
        this.swinging = buffer.readBoolean();
        if (this.swinging) this.swingingArm = buffer.readEnum(InteractionHand.class);
        else this.swingingArm = null;
        this.isShiftKeyDown = buffer.readBoolean();
        this.usingItem = buffer.readBoolean();
        this.usingHand = buffer.readEnum(InteractionHand.class);
    }

    @Override
    public void encodeInfoServerSide(RegistryFriendlyByteBuf buffer) {
        ItemStack.STREAM_CODEC.encode(buffer, this.mainHand);
        ItemStack.STREAM_CODEC.encode(buffer, this.offHand);
        buffer.writeBoolean(this.fallFlying);
        buffer.writeEnum(this.pose);
        buffer.writeFloat(this.yBodyRot);
        buffer.writeFloat(this.yHeadRot);
        buffer.writeBoolean(this.swinging);
        if (this.swinging) buffer.writeEnum(this.swingingArm);
        buffer.writeBoolean(this.isShiftKeyDown);
        buffer.writeBoolean(this.usingItem);
        buffer.writeEnum(this.usingHand);
    }
}
