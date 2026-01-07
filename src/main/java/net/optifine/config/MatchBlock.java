package net.optifine.config;

import net.minecraft.block.state.BlockStateBase;
import net.minecraft.src.Config;

public class MatchBlock {
    private final int blockId;
    private int[] metadatas = null;

    public MatchBlock(int blockId) {
        this.blockId = blockId;
    }

    public MatchBlock(int blockId, int metadata) {
        this.blockId = blockId;

        if (metadata >= 0 && metadata <= 15) {
            metadatas = new int[]{metadata};
        }
    }

    public MatchBlock(int blockId, int[] metadatas) {
        this.blockId = blockId;
        this.metadatas = metadatas;
    }

    public int getBlockId() {
        return blockId;
    }

    public int[] getMetadatas() {
        return metadatas;
    }

    public boolean matches(BlockStateBase blockState) {
        return blockState.getBlockId() == blockId && Matches.metadata(blockState.getMetadata(), metadatas);
    }

    public boolean matches(int id, int metadata) {
        return id == blockId && Matches.metadata(metadata, metadatas);
    }

    public void addMetadata(int metadata) {
        if (metadatas != null) {
            if (metadata >= 0 && metadata <= 15) {
                for (int j : metadatas) {
                    if (j == metadata) {
                        return;
                    }
                }

                metadatas = Config.addIntToArray(metadatas, metadata);
            }
        }
    }

    public String toString() {
        return blockId + ":" + Config.arrayToString(metadatas);
    }
}
