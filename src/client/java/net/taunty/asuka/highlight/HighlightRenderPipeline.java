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

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.taunty.asuka.keybinding.ModKeybinds;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

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

    private static final int SEARCH_RADIUS = 32;
    private static final int SCAN_WIDTH = SEARCH_RADIUS * 2 + 1;
    private static final int SCAN_VOLUME = SCAN_WIDTH * SCAN_WIDTH * SCAN_WIDTH;
    private static final int BLOCKS_PER_TICK = 8192;
    private static final int IDLE_REFRESH_TICKS = 100;

    private static ClientLevel scanWorld;
    private static BlockPos lastScanPos;
    private static BlockPos scanCenter;
    private static int scanIndex;
    private static int ticksSinceScan;
    private static final List<HighlightRenderState> pendingHighlights = new ArrayList<>();
    private static final BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();

    private static final Set<Block> NODE_BLOCKS = Set.of(
            Blocks.DYED_TERRACOTTA.magenta(),
            Blocks.DYED_TERRACOTTA.purple()
    );

    private static final Set<Block> MC_HEADS = Set.of(
            Blocks.CREEPER_HEAD,
            Blocks.CREEPER_WALL_HEAD,
            Blocks.PLAYER_HEAD,
            Blocks.PLAYER_WALL_HEAD
    );

    private static final Set<EntityType<?>> ENTITY_TYPES = Set.of(
            EntityTypes.ARMOR_STAND
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

    /**
     * Registers scan updates. Rendering runs from LevelRendererMixin after the level finishes.
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(HighlightRenderPipeline::tickHighlights);
    }

    /**
     * Clears visible highlights and pending scan progress, including the previous world and position.
     */
    private static void resetScan() {
        highlightNodes = List.of();
        pendingHighlights.clear();
        scanWorld = null;
        lastScanPos = null;
        scanCenter = null;
        scanIndex = 0;
        ticksSinceScan = 0;
    }

    /**
     * Scans a limited number of block positions each tick, keeping previous results visible
     * until the scan finishes. New scans start after movement or the idle refresh delay.
     * Disabling ESP, losing the player/world, changing worlds clears the old results and progress.
     * Entity checks run when the block scan finishes.
     *
     * @param client the client whose world and player are used for scanning
     */
    private static void tickHighlights(Minecraft client) {
        ClientLevel world = client.level;
        LocalPlayer player = client.player;
        if (!ModKeybinds.isEnabledNodeESP() || world == null || player == null) {
            resetScan();
            return;
        }

        BlockPos playerPos = player.blockPosition();
        BlockPos referencePos = scanCenter != null ? scanCenter : lastScanPos;
        if (world != scanWorld || (referencePos != null &&
                playerPos.distSqr(referencePos) > SEARCH_RADIUS * SEARCH_RADIUS)) {
            resetScan();
            scanWorld = world;
        }

        if (scanCenter == null) {
            ticksSinceScan++;
            if (lastScanPos != null &&
                    ticksSinceScan < IDLE_REFRESH_TICKS) {
                return;
            }
            scanCenter = playerPos.immutable();
            scanIndex = 0;
            pendingHighlights.clear();
        }

        // Finish this cube before following normal movement, so walking cannot restart it forever.
        int endIndex = Math.min(scanIndex + BLOCKS_PER_TICK, SCAN_VOLUME);
        for (; scanIndex < endIndex; scanIndex++) {
            int x = scanIndex / (SCAN_WIDTH * SCAN_WIDTH) - SEARCH_RADIUS;
            int y = (scanIndex / SCAN_WIDTH) % SCAN_WIDTH - SEARCH_RADIUS;
            int z = scanIndex % SCAN_WIDTH - SEARCH_RADIUS;
            scanPos.set(scanCenter.getX() + x, scanCenter.getY() + y, scanCenter.getZ() + z);
            BlockState state = world.getBlockState(scanPos);
            if (NODE_BLOCKS.contains(state.getBlock()) || MC_HEADS.contains(state.getBlock())) {
                pendingHighlights.add(new HighlightRenderState(
                        scanPos.getX(), scanPos.getY(), scanPos.getZ(),
                        1f, 0f, 0f, 0.5f
                ));
            }
        }

        if (scanIndex < SCAN_VOLUME) return;

        AABB searchBox = new AABB(scanCenter).inflate(SEARCH_RADIUS);
        for (Entity entity : world.getEntities((Entity) null, searchBox, e -> ENTITY_TYPES.contains(e.getType()))) {
            BlockPos entityPos = entity.blockPosition().offset(0, 2, 0);
            if (world.getBlockState(entityPos).is(Blocks.ICE)) {
                pendingHighlights.add(new HighlightRenderState(
                        entityPos.getX(), entityPos.getY(), entityPos.getZ(),
                        1f, 0f, 0f, 0.5f
                ));
            }
        }

        // Publish only complete results; the previous highlights stay visible during the scan.
        highlightNodes = List.copyOf(pendingHighlights);
        pendingHighlights.clear();
        lastScanPos = scanCenter;
        scanCenter = null;
        ticksSinceScan = 0;
    }

    /**
     * Builds and draws boxes for the latest completed scan, offset by the camera position.
     * Does nothing when there are no highlights.
     *
     * @param camera the current frame's camera position
     * @param modelViewMatrix the level's model-view transform
     */
    public static void drawHighlights(Vec3 camera, Matrix4fc modelViewMatrix) {
        if (highlightNodes.isEmpty()) return;

        RenderPipeline renderPipeline = FILLED_THROUGH_WALLS;
        VertexFormat formatBinding = renderPipeline.getVertexFormatBinding(0);
        assert formatBinding != null;

        PrimitiveTopology primitive = renderPipeline.getPrimitiveTopology();
        StagedVertexBuffer.Draw draw = stagedBuffer.appendDraw(formatBinding, primitive,
                primitive == PrimitiveTopology.QUADS ? RenderSystem.getProjectionType().vertexSorting() : null);

        PoseStack matrices = new PoseStack();

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
            draw(Minecraft.getInstance(), info, renderPipeline, modelViewMatrix);
        }

        stagedBuffer.endFrame();
    }

    /**
     * Adds the six coloured faces of a box to the vertex buffer without submitting a draw call.
     *
     * @param positionMatrix transform applied to each vertex
     * @param buffer destination for the box vertices
     * @param minX minimum world-space X coordinate
     * @param minY minimum world-space Y coordinate
     * @param minZ minimum world-space Z coordinate
     * @param maxX maximum world-space X coordinate
     * @param maxY maximum world-space Y coordinate
     * @param maxZ maximum world-space Z coordinate
     * @param red red colour component from 0 to 1
     * @param green green colour component from 0 to 1
     * @param blue blue colour component from 0 to 1
     * @param alpha opacity from 0 (transparent) to 1 (opaque)
     */
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

    /**
     * Submits uploaded highlight geometry to the client's main render target using the given pipeline.
     *
     * @param client the client providing the main render target
     * @param info uploaded vertex/index buffers and draw offsets
     * @param pipeline rendering settings used to draw the geometry
     * @param modelViewMatrix the level transform, since its global matrix has already been popped
     */
    private static void draw(Minecraft client, StagedVertexBuffer.ExecuteInfo info, RenderPipeline pipeline, Matrix4fc modelViewMatrix) {
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(new Matrix4f(modelViewMatrix), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

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
