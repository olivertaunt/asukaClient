package net.taunty.asuka.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.taunty.asuka.highlight.HandheldAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyReturnValue(method = "getCurrentSwingDuration", at = @At("RETURN"))
    private int asuka$swingDuration(int original) {
        if (HandheldAnimation.isEnabled()) {
            float swingDuration = HandheldAnimation.getSwingDuration();
            if ((LivingEntity) (Object) this == Minecraft.getInstance().player) {
                return Math.max(1, Math.round(original * swingDuration));
            }
            return original;
        } else {
            return original;
        }
    }

}
