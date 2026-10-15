package net.taunty.asuka.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen{

    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("asuka", "textures/gui/asuka.png");

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Redirect(method = "extractRenderState", at = @At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/TitleScreen;extractPanorama(Lnet/minecraft/client/gui/GuiGraphicsExtractor;F)V"))
    private void asukaBackground(TitleScreen screen, GuiGraphicsExtractor graphics, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, 0f, 0f, this.width, this.height, 2560,1440, 2560, 1440);
    }
}
