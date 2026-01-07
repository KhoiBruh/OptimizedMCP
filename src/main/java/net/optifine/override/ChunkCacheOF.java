package net.optifine.override;

import net.minecraft.block.state.IBlockState;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.DynamicLights;
import net.optifine.util.ArrayCache;

import java.util.Arrays;

public class ChunkCacheOF implements IBlockAccess {
    private static final ArrayCache cacheCombinedLights = new ArrayCache(Integer.TYPE, 16);
    private static final ArrayCache cacheBlockStates = new ArrayCache(IBlockState.class, 16);
    private final ChunkCache chunkCache;
    private final int posX;
    private final int posY;
    private final int posZ;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final int sizeXY;
    private final int arraySize;
    private final boolean dynamicLights = Config.isDynamicLights();
    private int[] combinedLights;
    private IBlockState[] blockStates;

    public ChunkCacheOF(ChunkCache chunkCache, BlockPos posFromIn, BlockPos posToIn, int subIn) {
        this.chunkCache = chunkCache;
        int i = posFromIn.getX() - subIn >> 4;
        int j = posFromIn.getY() - subIn >> 4;
        int k = posFromIn.getZ() - subIn >> 4;
        int l = posToIn.getX() + subIn >> 4;
        int i1 = posToIn.getY() + subIn >> 4;
        int j1 = posToIn.getZ() + subIn >> 4;
        sizeX = l - i + 1 << 4;
        sizeY = i1 - j + 1 << 4;
        sizeZ = j1 - k + 1 << 4;
        sizeXY = sizeX * sizeY;
        arraySize = sizeX * sizeY * sizeZ;
        posX = i << 4;
        posY = j << 4;
        posZ = k << 4;
    }

    private int getPositionIndex(BlockPos pos) {
        int i = pos.getX() - posX;

        if (i >= 0 && i < sizeX) {
            int j = pos.getY() - posY;

            if (j >= 0 && j < sizeY) {
                int k = pos.getZ() - posZ;
                return k >= 0 && k < sizeZ ? k * sizeXY + j * sizeX + i : -1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public int getCombinedLight(BlockPos pos, int lightValue) {
        int i = getPositionIndex(pos);

        if (i >= 0 && i < arraySize && combinedLights != null) {
            int j = combinedLights[i];

            if (j == -1) {
                j = getCombinedLightRaw(pos, lightValue);
                combinedLights[i] = j;
            }

            return j;
        } else {
            return getCombinedLightRaw(pos, lightValue);
        }
    }

    private int getCombinedLightRaw(BlockPos pos, int lightValue) {
        int i = chunkCache.getCombinedLight(pos, lightValue);

        if (dynamicLights && !getBlockState(pos).getBlock().isOpaqueCube()) {
            i = DynamicLights.getCombinedLight(pos, i);
        }

        return i;
    }

    public IBlockState getBlockState(BlockPos pos) {
        int i = getPositionIndex(pos);

        if (i >= 0 && i < arraySize && blockStates != null) {
            IBlockState iblockstate = blockStates[i];

            if (iblockstate == null) {
                iblockstate = chunkCache.getBlockState(pos);
                blockStates[i] = iblockstate;
            }

            return iblockstate;
        } else {
            return chunkCache.getBlockState(pos);
        }
    }

    public void renderStart() {
        if (combinedLights == null) {
            combinedLights = (int[]) cacheCombinedLights.allocate(arraySize);
        }

        Arrays.fill(combinedLights, -1);

        if (blockStates == null) {
            blockStates = (IBlockState[]) cacheBlockStates.allocate(arraySize);
        }

        Arrays.fill(blockStates, null);
    }

    public void renderFinish() {
        cacheCombinedLights.free(combinedLights);
        combinedLights = null;
        cacheBlockStates.free(blockStates);
        blockStates = null;
    }

    public boolean extendedLevelsInChunkCache() {
        return chunkCache.extendedLevelsInChunkCache();
    }

    public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
        return chunkCache.getBiomeGenForCoords(pos);
    }

    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return chunkCache.getStrongPower(pos, direction);
    }

    public TileEntity getTileEntity(BlockPos pos) {
        return chunkCache.getTileEntity(pos);
    }

    public WorldType getWorldType() {
        return chunkCache.getWorldType();
    }

    public boolean isAirBlock(BlockPos pos) {
        return chunkCache.isAirBlock(pos);
    }

}
