package mod.syconn.svc.utils.interfaces;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

public interface IModifiedPoseRenderer {

    HashMap<Class<? extends Item>, IModifiedPoseRenderer> REGISTRY = new HashMap<>();

    static void register(Class<? extends Item> item, IModifiedPoseRenderer pose) {
        REGISTRY.put(item, pose);
    }

    void modifyPose(LivingEntity entity, InteractionHand hand, ItemStack stack, HumanoidModel<? extends LivingEntity> model, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float tickDelta);
}
