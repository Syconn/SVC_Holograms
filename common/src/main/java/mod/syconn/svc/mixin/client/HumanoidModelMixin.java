package mod.syconn.svc.mixin.client;

import mod.syconn.svc.client.SVCClient;
import mod.syconn.svc.utils.interfaces.IModifiedPoseRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
@Environment(EnvType.CLIENT)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {

    @SuppressWarnings("unchecked")
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "TAIL"))
    public void setAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity.isSwimming()) return;

        var model = (HumanoidModel<T>) (Object) this;

        for (var hand : InteractionHand.values()) {
            var stack = entity.getItemInHand(hand);
            if (!stack.isEmpty()) {
                final IModifiedPoseRenderer pose = IModifiedPoseRenderer.REGISTRY.get(stack.getItem().getClass());
                if (pose != null) {
                    pose.modifyPose(entity, hand, stack, model, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, SVCClient.getTickDelta());
                    break;
                }
            }
        }
    }
}
