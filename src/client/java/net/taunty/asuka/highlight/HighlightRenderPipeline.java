package net.taunty.asuka.highlight;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColorCollection;
import net.taunty.asuka.keybinding.ModKeybinds;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.taunty.asuka.Asuka;

import java.util.*;

public class HighlightRenderPipeline {

    private static final Set<Object> NODE_BLOCKS = Set.of(
            Blocks.DYED_TERRACOTTA.magenta(),
            Blocks.DYED_TERRACOTTA.purple()
    );

    private static final Set<Object> MC_HEADS = Set.of(
            Blocks.CREEPER_HEAD,
            Blocks.CREEPER_WALL_HEAD,
            Blocks.PLAYER_HEAD,
            Blocks.PLAYER_WALL_HEAD
    );


    private static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Asuka.id("pipeline/highlight_through_walls"))
                    .withDepthStencilState(Optional.empty())
                    .build()
    );

    private static List<HighlightRenderState> highlightNodes = new ArrayList<>();

    private record HighlightRenderState(int x, int y, int z, float r, float g, float b, float a) { }

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final StagedVertexBuffer stagedBuffer =
            new StagedVertexBuffer(() -> "Highlight Buffer", 256);

    public static void register() {
        LevelExtractionEvents.END_EXTRACTION.register(HighlightRenderPipeline::extractHighlights);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(HighlightRenderPipeline::drawHighlights);
    }

    private static void extractHighlights(LevelExtractionContext context) {
        if (!ModKeybinds.isEnabledNodeESP()){
            highlightNodes = new ArrayList<>();
            return;
        }
//        if (!ModKeybinds.isEnabledGiftESP()){

//        }
        ClientLevel world = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;

        if (world == null || player == null) return;

        List<HighlightRenderState> found = new ArrayList<>();
        BlockPos playerPos = player.blockPosition();
        int searchRadius = 32; // blocks, tune this later

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    pos.set(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);
                    BlockState state = world.getBlockState(pos);


                    if (NODE_BLOCKS.contains(state.getBlock()) || MC_HEADS.contains(state.getBlock())) {
                        found.add(new HighlightRenderState(
                                pos.getX(), pos.getY(), pos.getZ(),
                                1f, 0f, 0f, 0.5f
                        ));
                    }
                }
            }
        }

        highlightNodes = found;
    }

    private static void drawHighlights(LevelRenderContext context) {
        if (highlightNodes.isEmpty()) return;

        RenderPipeline renderPipeline = FILLED_THROUGH_WALLS;
        VertexFormat formatBinding = renderPipeline.getVertexFormatBinding(0);
        assert formatBinding != null;

        PrimitiveTopology primitive = renderPipeline.getPrimitiveTopology();
        StagedVertexBuffer.Draw draw = stagedBuffer.appendDraw(formatBinding, primitive,
                primitive == PrimitiveTopology.QUADS ? RenderSystem.getProjectionType().vertexSorting() : null);

        PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        var builder = stagedBuffer.getVertexBuilder(draw);

        for (HighlightRenderState h : highlightNodes) {
            renderFilledBox(matrices.last().pose(), builder,
                    h.x(), h.y(), h.z(), h.x() + 1, h.y() + 1, h.z() + 1,
                    h.r(), h.g(), h.b(), h.a());
        }

        matrices.popPose();

        stagedBuffer.upload();
        StagedVertexBuffer.ExecuteInfo info = stagedBuffer.getExecuteInfo(draw);

        if (info != null) {
            draw(Minecraft.getInstance(), info, renderPipeline);
        }

        stagedBuffer.endFrame();
    }

    private static void renderFilledBox(Matrix4fc positionMatrix, VertexConsumer buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha) {
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);

        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);

        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);

        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
    }

    private static void draw(Minecraft client, StagedVertexBuffer.ExecuteInfo info, RenderPipeline pipeline) {
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        RenderTarget mainTarget = client.gameRenderer.mainRenderTarget();
        GpuTextureView colorTexture = mainTarget.getColorTextureView();
        assert colorTexture != null;

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> Asuka.MOD_ID + " highlight render", colorTexture, Optional.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
            renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());
            renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }
}