package org.dxvprojects.nomoretotems.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(
            method = "damage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void disableTotemAnyDamage(CallbackInfoReturnable<?> cir) {
        // always prevent totem usage: clear totem from hands
        LivingEntity self = (LivingEntity)(Object)this;
        ItemStack main = self.getMainHandStack();
        ItemStack off = self.getOffHandStack();
        if (main.isOf(Items.TOTEM_OF_UNDYING)) {
            self.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
        if (off.isOf(Items.TOTEM_OF_UNDYING)) {
            self.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
        }
        // don't cancel damage here; just remove the totem so revive logic fails
    }
}
