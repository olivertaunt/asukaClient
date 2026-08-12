package net.taunty.asuka.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.taunty.asuka.Asuka;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.UUID;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin {

    @Shadow
    public abstract com.mojang.authlib.GameProfile getProfile();

    private static final ClientAsset.ResourceTexture MY_CAPE =
            new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("asuka", "cape/asuka2"));
    private static final PlayerSkin.Patch CAPE_PATCH =
            new PlayerSkin.Patch(Optional.empty(), Optional.of(MY_CAPE), Optional.empty(), Optional.empty());

    @ModifyReturnValue(method = "getSkin", at = @At("RETURN"))
    private PlayerSkin asuka$applyCustomCape(PlayerSkin original) {
        if (this.getProfile().id().equals(net.minecraft.client.Minecraft.getInstance().getUser().getProfileId())) {
            return original.with(CAPE_PATCH);
        }
        return original;
    }
}