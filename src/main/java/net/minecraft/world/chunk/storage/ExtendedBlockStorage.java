package net.minecraft.world.chunk.storage;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.NibbleArray;

public class ExtendedBlockStorage {
    private final int yBase;
    private int blockRefCount;
    private int tickRefCount;
    private char[] data;
    private NibbleArray blocklightArray;
    private NibbleArray skylightArray;

    public ExtendedBlockStorage(int y, boolean storeSkylight) {
        yBase = y;
        data = new char[4096];
        blocklightArray = new NibbleArray();

        if (storeSkylight) {
            skylightArray = new NibbleArray();
        }
    }

    public IBlockState get(int x, int y, int z) {
        IBlockState iblockstate = Block.BLOCK_STATE_IDS.getByValue(data[y << 8 | z << 4 | x]);
        return iblockstate != null ? iblockstate : Blocks.air.getDefaultState();
    }

    public void set(int x, int y, int z, IBlockState state) {
        IBlockState iblockstate = get(x, y, z);
        Block block = iblockstate.getBlock();
        Block block1 = state.getBlock();

        if (block != Blocks.air) {
            --blockRefCount;

            if (block.getTickRandomly()) {
                --tickRefCount;
            }
        }

        if (block1 != Blocks.air) {
            ++blockRefCount;

            if (block1.getTickRandomly()) {
                ++tickRefCount;
            }
        }

        data[y << 8 | z << 4 | x] = (char) Block.BLOCK_STATE_IDS.get(state);
    }

    public Block getBlockByExtId(int x, int y, int z) {
        return get(x, y, z).getBlock();
    }

    public int getExtBlockMetadata(int x, int y, int z) {
        IBlockState iblockstate = get(x, y, z);
        return iblockstate.getBlock().getMetaFromState(iblockstate);
    }

    public boolean isEmpty() {
        return blockRefCount == 0;
    }

    public boolean getNeedsRandomTick() {
        return tickRefCount > 0;
    }

    public int getYLocation() {
        return yBase;
    }

    public void setExtSkylightValue(int x, int y, int z, int value) {
        skylightArray.set(x, y, z, value);
    }

    public int getExtSkylightValue(int x, int y, int z) {
        return skylightArray.get(x, y, z);
    }

    public void setExtBlocklightValue(int x, int y, int z, int value) {
        blocklightArray.set(x, y, z, value);
    }

    public int getExtBlocklightValue(int x, int y, int z) {
        return blocklightArray.get(x, y, z);
    }

    public void removeInvalidBlocks() {
        IBlockState iblockstate = Blocks.air.getDefaultState();
        int i = 0;
        int j = 0;

        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                for (int i1 = 0; i1 < 16; ++i1) {
                    Block block = getBlockByExtId(i1, k, l);

                    if (block != Blocks.air) {
                        ++i;

                        if (block.getTickRandomly()) {
                            ++j;
                        }
                    }
                }
            }
        }

        blockRefCount = i;
        tickRefCount = j;
    }

    public char[] getData() {
        return data;
    }

    public void setData(char[] dataArray) {
        data = dataArray;
    }

    public NibbleArray getBlocklightArray() {
        return blocklightArray;
    }

    public void setBlocklightArray(NibbleArray newBlocklightArray) {
        blocklightArray = newBlocklightArray;
    }

    public NibbleArray getSkylightArray() {
        return skylightArray;
    }

    public void setSkylightArray(NibbleArray newSkylightArray) {
        skylightArray = newSkylightArray;
    }

    public int getBlockRefCount() {
        return blockRefCount;
    }
}
