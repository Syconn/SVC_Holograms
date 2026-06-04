package mod.syconn.svc.mixin.client;

import mod.syconn.svc.utils.generic.RenderUtil;
import mod.syconn.svc.utils.interfaces.IItemExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Final
    @Shadow
    private Minecraft minecraft;
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private ItemStack offHandItem;
    @Shadow
    private float oOffHandHeight;
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float offHandHeight;

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void tick(CallbackInfo cir) {
        var localPlayer = this.minecraft.player;
        if (localPlayer != null) {
            var itemStack = localPlayer.getMainHandItem();
            var itemStack2 = localPlayer.getOffhandItem();

            if (itemStack.getItem() instanceof IItemExtensions) {
                this.oMainHandHeight = this.mainHandHeight;

                if (ItemStack.matches(this.mainHandItem, itemStack)) this.mainHandItem = itemStack;

                if (localPlayer.isHandsBusy()) this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4f, 0.0f, 1.0f);
                else {
                    var f = localPlayer.getAttackStrengthScale(1.0f);
                    var reequipM = RenderUtil.handleReequipAnimation(this.mainHandItem, itemStack, localPlayer.getInventory().selected);
                    if (!reequipM && this.mainHandItem != itemStack) this.mainHandItem = itemStack;
                    this.mainHandHeight += Mth.clamp((!reequipM ? f * f * f : 0.0f) - this.mainHandHeight, -0.4f, 0.4f);
                }

                if (this.mainHandHeight < 0.1F) this.mainHandItem = itemStack;
            }

            if (itemStack2.getItem() instanceof IItemExtensions) {
                this.oOffHandHeight = this.offHandHeight;

                if (ItemStack.matches(this.offHandItem, itemStack2))this.offHandItem = itemStack2;

                if (localPlayer.isHandsBusy()) this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4f, 0.0f, 1.0f);
                else {
                    var reequipO = RenderUtil.handleReequipAnimation(this.offHandItem, itemStack2, -1);
                    if (!reequipO && this.offHandItem != itemStack2) this.offHandItem = itemStack2;
                    this.offHandHeight += Mth.clamp((float)(!reequipO ? 1 : 0) - this.offHandHeight, -0.4f, 0.4f);
                }

                if (this.offHandHeight < 0.1F) this.offHandItem = itemStack2;
            }
        }
    }
}
