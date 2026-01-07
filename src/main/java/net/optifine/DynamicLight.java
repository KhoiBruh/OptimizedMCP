package net.optifine;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class DynamicLight {
    private final Entity entity;
    private final double offsetY;
    private double lastPosX = -2.147483648E9D;
    private double lastPosY = -2.147483648E9D;
    private double lastPosZ = -2.147483648E9D;
    private int lastLightLevel = 0;
    private boolean underwater = false;
    private long timeCheckMs = 0L;
    private Set<BlockPos> setLitChunkPos = new HashSet<>();
    private final BlockPos.MutableBlockPos blockPosMutable = new BlockPos.MutableBlockPos();

    public DynamicLight(Entity entity) {
        this.entity = entity;
        offsetY = entity.getEyeHeight();
    }

    public void update(RenderGlobal renderGlobal) {
        if (Config.isDynamicLightsFast()) {
            long i = System.currentTimeMillis();

            if (i < timeCheckMs + 500L) {
                return;
            }

            timeCheckMs = i;
        }

        double d6 = entity.posX - 0.5D;
        double d0 = entity.posY - 0.5D + offsetY;
        double d1 = entity.posZ - 0.5D;
        int j = DynamicLights.getLightLevel(entity);
        double d2 = d6 - lastPosX;
        double d3 = d0 - lastPosY;
        double d4 = d1 - lastPosZ;
        double d5 = 0.1D;

        if (Math.abs(d2) > d5 || Math.abs(d3) > d5 || Math.abs(d4) > d5 || lastLightLevel != j) {
            lastPosX = d6;
            lastPosY = d0;
            lastPosZ = d1;
            lastLightLevel = j;
            underwater = false;
            World world = renderGlobal.getWorld();

            if (world != null) {
                blockPosMutable.set(MathHelper.floor_double(d6), MathHelper.floor_double(d0), MathHelper.floor_double(d1));
                IBlockState iblockstate = world.getBlockState(blockPosMutable);
                Block block = iblockstate.getBlock();
                underwater = block == Blocks.water;
            }

            Set<BlockPos> set = new HashSet<>();

            if (j > 0) {
                EnumFacing enumfacing2 = (MathHelper.floor_double(d6) & 15) >= 8 ? EnumFacing.EAST : EnumFacing.WEST;
                EnumFacing enumfacing = (MathHelper.floor_double(d0) & 15) >= 8 ? EnumFacing.UP : EnumFacing.DOWN;
                EnumFacing enumfacing1 = (MathHelper.floor_double(d1) & 15) >= 8 ? EnumFacing.SOUTH : EnumFacing.NORTH;
                BlockPos blockpos = new BlockPos(d6, d0, d1);
                RenderChunk renderchunk = renderGlobal.getRenderChunk(blockpos);
                BlockPos blockpos1 = getChunkPos(renderchunk, blockpos, enumfacing2);
                RenderChunk renderchunk1 = renderGlobal.getRenderChunk(blockpos1);
                BlockPos blockpos2 = getChunkPos(renderchunk, blockpos, enumfacing1);
                RenderChunk renderchunk2 = renderGlobal.getRenderChunk(blockpos2);
                BlockPos blockpos3 = getChunkPos(renderchunk1, blockpos1, enumfacing1);
                RenderChunk renderchunk3 = renderGlobal.getRenderChunk(blockpos3);
                BlockPos blockpos4 = getChunkPos(renderchunk, blockpos, enumfacing);
                RenderChunk renderchunk4 = renderGlobal.getRenderChunk(blockpos4);
                BlockPos blockpos5 = getChunkPos(renderchunk4, blockpos4, enumfacing2);
                RenderChunk renderchunk5 = renderGlobal.getRenderChunk(blockpos5);
                BlockPos blockpos6 = getChunkPos(renderchunk4, blockpos4, enumfacing1);
                RenderChunk renderchunk6 = renderGlobal.getRenderChunk(blockpos6);
                BlockPos blockpos7 = getChunkPos(renderchunk5, blockpos5, enumfacing1);
                RenderChunk renderchunk7 = renderGlobal.getRenderChunk(blockpos7);
                updateChunkLight(renderchunk, setLitChunkPos, set);
                updateChunkLight(renderchunk1, setLitChunkPos, set);
                updateChunkLight(renderchunk2, setLitChunkPos, set);
                updateChunkLight(renderchunk3, setLitChunkPos, set);
                updateChunkLight(renderchunk4, setLitChunkPos, set);
                updateChunkLight(renderchunk5, setLitChunkPos, set);
                updateChunkLight(renderchunk6, setLitChunkPos, set);
                updateChunkLight(renderchunk7, setLitChunkPos, set);
            }

            updateLitChunks(renderGlobal);
            setLitChunkPos = set;
        }
    }

    private BlockPos getChunkPos(RenderChunk renderChunk, BlockPos pos, EnumFacing facing) {
        return renderChunk != null ? renderChunk.getBlockPosOffset16(facing) : pos.offset(facing, 16);
    }

    private void updateChunkLight(RenderChunk renderChunk, Set<BlockPos> setPrevPos, Set<BlockPos> setNewPos) {
        if (renderChunk != null) {
            CompiledChunk compiledchunk = renderChunk.getCompiledChunk();

            if (compiledchunk != null && !compiledchunk.isEmpty()) {
                renderChunk.setNeedsUpdate(true);
            }

            BlockPos blockpos = renderChunk.getPosition();

            if (setPrevPos != null) {
                setPrevPos.remove(blockpos);
            }

            if (setNewPos != null) {
                setNewPos.add(blockpos);
            }
        }
    }

    public void updateLitChunks(RenderGlobal renderGlobal) {
        for (BlockPos blockpos : setLitChunkPos) {
            RenderChunk renderchunk = renderGlobal.getRenderChunk(blockpos);
            updateChunkLight(renderchunk, null, null);
        }
    }

    public double getLastPosX() {
        return lastPosX;
    }

    public double getLastPosY() {
        return lastPosY;
    }

    public double getLastPosZ() {
        return lastPosZ;
    }

    public int getLastLightLevel() {
        return lastLightLevel;
    }

    public boolean isUnderwater() {
        return underwater;
    }

    public String toString() {
        return "Entity: " + entity + ", offsetY: " + offsetY;
    }
}
