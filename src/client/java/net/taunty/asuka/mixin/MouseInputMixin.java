package net.taunty.asuka.mixin;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.taunty.asuka.player.freecam.Freecam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public abstract class MouseInputMixin  {
    @Redirect(method="turnPlayer", at= @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void asuka$rotateCamera(LocalPlayer player, double yaw, double pitch) {
        if (Freecam.isEnabled()) {
            Freecam.rotateCamera(yaw, pitch);
        } else {
            player.turn(yaw, pitch);
        }
    }

}
