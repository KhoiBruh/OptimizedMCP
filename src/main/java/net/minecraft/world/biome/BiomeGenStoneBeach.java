package net.minecraft.world.biome;

import net.minecraft.init.Blocks;

public class BiomeGenStoneBeach extends BiomeGenBase {
    public BiomeGenStoneBeach(int id) {
        super(id);
        spawnableCreatureList.clear();
        topBlock = Blocks.stone.getDefaultState();
        fillerBlock = Blocks.stone.getDefaultState();
        theBiomeDecorator.treesPerChunk = -999;
        theBiomeDecorator.deadBushPerChunk = 0;
        theBiomeDecorator.reedsPerChunk = 0;
        theBiomeDecorator.cactiPerChunk = 0;
    }
}
