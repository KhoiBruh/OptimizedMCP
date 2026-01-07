package net.minecraft.world.biome;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.*;

import java.util.Random;

public class BiomeGenTaiga extends BiomeGenBase {
    private static final WorldGenTaiga1 field_150639_aC = new WorldGenTaiga1();
    private static final WorldGenTaiga2 field_150640_aD = new WorldGenTaiga2(false);
    private static final WorldGenMegaPineTree field_150641_aE = new WorldGenMegaPineTree(false, false);
    private static final WorldGenMegaPineTree field_150642_aF = new WorldGenMegaPineTree(false, true);
    private static final WorldGenBlockBlob field_150643_aG = new WorldGenBlockBlob(Blocks.mossy_cobblestone, 0);
    private final int field_150644_aH;

    public BiomeGenTaiga(int id, int p_i45385_2_) {
        super(id);
        field_150644_aH = p_i45385_2_;
        spawnableCreatureList.add(new BiomeGenBase.SpawnListEntry(EntityWolf.class, 8, 4, 4));
        theBiomeDecorator.treesPerChunk = 10;

        if (p_i45385_2_ != 1 && p_i45385_2_ != 2) {
            theBiomeDecorator.grassPerChunk = 1;
            theBiomeDecorator.mushroomsPerChunk = 1;
        } else {
            theBiomeDecorator.grassPerChunk = 7;
            theBiomeDecorator.deadBushPerChunk = 1;
            theBiomeDecorator.mushroomsPerChunk = 3;
        }
    }

    public WorldGenAbstractTree genBigTreeChance(Random rand) {
        return (field_150644_aH == 1 || field_150644_aH == 2) && rand.nextInt(3) == 0 ? (field_150644_aH != 2 && rand.nextInt(13) != 0 ? field_150641_aE : field_150642_aF) : (rand.nextInt(3) == 0 ? field_150639_aC : field_150640_aD);
    }

    public WorldGenerator getRandomWorldGenForGrass(Random rand) {
        return rand.nextInt(5) > 0 ? new WorldGenTallGrass(BlockTallGrass.EnumType.FERN) : new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS);
    }

    public void decorate(World worldIn, Random rand, BlockPos pos) {
        if (field_150644_aH == 1 || field_150644_aH == 2) {
            int i = rand.nextInt(3);

            for (int j = 0; j < i; ++j) {
                int k = rand.nextInt(16) + 8;
                int l = rand.nextInt(16) + 8;
                BlockPos blockpos = worldIn.getHeight(pos.add(k, 0, l));
                field_150643_aG.generate(worldIn, rand, blockpos);
            }
        }

        DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.FERN);

        for (int i1 = 0; i1 < 7; ++i1) {
            int j1 = rand.nextInt(16) + 8;
            int k1 = rand.nextInt(16) + 8;
            int l1 = rand.nextInt(worldIn.getHeight(pos.add(j1, 0, k1)).getY() + 32);
            DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, pos.add(j1, l1, k1));
        }

        super.decorate(worldIn, rand, pos);
    }

    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        if (field_150644_aH == 1 || field_150644_aH == 2) {
            topBlock = Blocks.grass.getDefaultState();
            fillerBlock = Blocks.dirt.getDefaultState();

            if (noiseVal > 1.75D) {
                topBlock = Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
            } else if (noiseVal > -0.95D) {
                topBlock = Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
            }
        }

        generateBiomeTerrain(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
    }

    protected BiomeGenBase createMutatedBiome(int p_180277_1_) {
        return biomeID == BiomeGenBase.megaTaiga.biomeID ? (new BiomeGenTaiga(p_180277_1_, 2)).func_150557_a(5858897, true).setBiomeName("Mega Spruce Taiga").setFillerBlockMetadata(5159473).setTemperatureRainfall(0.25F, 0.8F).setHeight(new BiomeGenBase.Height(minHeight, maxHeight)) : super.createMutatedBiome(p_180277_1_);
    }
}
