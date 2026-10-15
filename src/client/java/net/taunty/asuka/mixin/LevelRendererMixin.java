package net.taunty.asuka.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.taunty.asuka.highlight.HighlightRenderPipeline;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    // Iris finishes compositing before render returns, so the highlights stay above the shader output.
    @Inject(method = "render", at = @At("RETURN"))
    private void asuka$drawHighlights(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker,
                                      boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix,
                                      GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky,
                                      CallbackInfo ci) {
        HighlightRenderPipeline.drawHighlights(cameraState.pos, modelViewMatrix);
    }
}
