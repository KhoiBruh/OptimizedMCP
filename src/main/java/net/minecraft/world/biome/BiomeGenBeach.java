package net.minecraft.world.biome;

import net.minecraft.init.Blocks;

public class BiomeGenBeach extends BiomeGenBase {
    public BiomeGenBeach(int id) {
        super(id);
        spawnableCreatureList.clear();
        topBlock = Blocks.sand.getDefaultState();
        fillerBlock = Blocks.sand.getDefaultState();
        theBiomeDecorator.treesPerChunk = -999;
        theBiomeDecorator.deadBushPerChunk = 0;
        theBiomeDecorator.reedsPerChunk = 0;
        theBiomeDecorator.cactiPerChunk = 0;
    }
}
