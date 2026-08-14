package net.taunty.asuka.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class GhostBlock {
    private static final GhostBlock INSTANCE = new GhostBlock();
    private boolean enabled = false;
    public static GhostBlock get(){
        return INSTANCE;
    }

    public boolean isEnabled(){
        return enabled;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
        if(!enabled){
            restoreAll();
        }
    }

    public void toggle(){
        setEnabled(!enabled);
    }

    private final Map<BlockPos, BlockState> ghostedBlocks = new LinkedHashMap<>();

    /***
     * checks and such
     */
    public void ghostTargetedBlock(){
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;
        if (!(client.hitResult instanceof BlockHitResult hit)) return; // if looking at a block
        if (hit.getType() != HitResult.Type.BLOCK) return; // extra check

        BlockPos pos = hit.getBlockPos();
        ghost(client.level, pos);
    }

    /**
     * method to ghost the block
     * @param world mc client level
     * @param pos location of block
     */
    public void ghost(ClientLevel world, BlockPos pos){
        if(ghostedBlocks.containsKey(pos)) return; // already ghosted

        BlockState original = world.getBlockState(pos);
        if(original.isAir()) return; // nothing to ghost block

        ghostedBlocks.put(pos, original); // add position and block to saved map

        world.setServerVerifiedBlockState(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS); // ghost it!
    }

    /***
     * reset one block
     * @param pos block looking at
     */
    public void restoreSingle(BlockPos pos){
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        BlockState original = ghostedBlocks.remove(pos);
        if (original!= null){
            client.level.setServerVerifiedBlockState(pos, original, Block.UPDATE_CLIENTS);
        }
    }

    /***
     * restores all blocks to original state
     */
    public void restoreAll(){
        Minecraft client = Minecraft.getInstance();
        if (client.level == null){
            ghostedBlocks.clear();
            return;
        }
        for(Map.Entry<BlockPos, BlockState> entry : ghostedBlocks.entrySet()){
            client.level.setServerVerifiedBlockState(entry.getKey(), entry.getValue(), Block.UPDATE_CLIENTS);
        }
        ghostedBlocks.clear();

    }

    public boolean isGhosted(BlockPos pos){
        return ghostedBlocks.containsKey(pos);
    }
}
