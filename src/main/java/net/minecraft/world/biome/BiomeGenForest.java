package net.minecraft.world.biome;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraft.world.gen.feature.WorldGenForest;

import java.util.Random;

public class BiomeGenForest extends BiomeGenBase {
    protected static final WorldGenForest field_150629_aC = new WorldGenForest(false, true);
    protected static final WorldGenForest field_150630_aD = new WorldGenForest(false, false);
    protected static final WorldGenCanopyTree field_150631_aE = new WorldGenCanopyTree(false);
    private final int field_150632_aF;

    public BiomeGenForest(int id, int p_i45377_2_) {
        super(id);
        field_150632_aF = p_i45377_2_;
        theBiomeDecorator.treesPerChunk = 10;
        theBiomeDecorator.grassPerChunk = 2;

        if (field_150632_aF == 1) {
            theBiomeDecorator.treesPerChunk = 6;
            theBiomeDecorator.flowersPerChunk = 100;
            theBiomeDecorator.grassPerChunk = 1;
        }

        setFillerBlockMetadata(5159473);
        setTemperatureRainfall(0.7F, 0.8F);

        if (field_150632_aF == 2) {
            field_150609_ah = 353825;
            color = 3175492;
            setTemperatureRainfall(0.6F, 0.6F);
        }

        if (field_150632_aF == 0) {
            spawnableCreatureList.add(new BiomeGenBase.SpawnListEntry(EntityWolf.class, 5, 4, 4));
        }

        if (field_150632_aF == 3) {
            theBiomeDecorator.treesPerChunk = -999;
        }
    }

    protected BiomeGenBase func_150557_a(int colorIn, boolean p_150557_2_) {
        if (field_150632_aF == 2) {
            field_150609_ah = 353825;
            color = colorIn;

            if (p_150557_2_) {
                field_150609_ah = (field_150609_ah & 16711422) >> 1;
            }

            return this;
        } else {
            return super.func_150557_a(colorIn, p_150557_2_);
        }
    }

    public WorldGenAbstractTree genBigTreeChance(Random rand) {
        return field_150632_aF == 3 && rand.nextInt(3) > 0 ? field_150631_aE : (field_150632_aF != 2 && rand.nextInt(5) != 0 ? worldGeneratorTrees : field_150630_aD);
    }

    public BlockFlower.FlowerType pickRandomFlower(Random rand, BlockPos pos) {
        if (field_150632_aF == 1) {
            double d0 = MathHelper.clamp((1.0D + GRASS_COLOR_NOISE.func_151601_a((double) pos.getX() / 48.0D, (double) pos.getZ() / 48.0D)) / 2.0D, 0.0D, 0.9999D);
            BlockFlower.FlowerType blockflower$enumflowertype = BlockFlower.FlowerType.values()[(int) (d0 * (double) BlockFlower.FlowerType.values().length)];
            return blockflower$enumflowertype == BlockFlower.FlowerType.BLUE_ORCHID ? BlockFlower.FlowerType.POPPY : blockflower$enumflowertype;
        } else {
            return super.pickRandomFlower(rand, pos);
        }
    }

    public void decorate(World worldIn, Random rand, BlockPos pos) {
        if (field_150632_aF == 3) {
            for (int i = 0; i < 4; ++i) {
                for (int j = 0; j < 4; ++j) {
                    int k = i * 4 + 1 + 8 + rand.nextInt(3);
                    int l = j * 4 + 1 + 8 + rand.nextInt(3);
                    BlockPos blockpos = worldIn.getHeight(pos.add(k, 0, l));

                    if (rand.nextInt(20) == 0) {
                        WorldGenBigMushroom worldgenbigmushroom = new WorldGenBigMushroom();
                        worldgenbigmushroom.generate(worldIn, rand, blockpos);
                    } else {
                        WorldGenAbstractTree worldgenabstracttree = genBigTreeChance(rand);
                        worldgenabstracttree.func_175904_e();

                        if (worldgenabstracttree.generate(worldIn, rand, blockpos)) {
                            worldgenabstracttree.func_180711_a(worldIn, rand, blockpos);
                        }
                    }
                }
            }
        }

        int j1 = rand.nextInt(5) - 3;

        if (field_150632_aF == 1) {
            j1 += 2;
        }

        for (int k1 = 0; k1 < j1; ++k1) {
            int l1 = rand.nextInt(3);

            if (l1 == 0) {
                DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.PlantType.SYRINGA);
            } else if (l1 == 1) {
                DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.PlantType.ROSE);
            } else {
                DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.PlantType.PAEONIA);
            }

            for (int i2 = 0; i2 < 5; ++i2) {
                int j2 = rand.nextInt(16) + 8;
                int k2 = rand.nextInt(16) + 8;
                int i1 = rand.nextInt(worldIn.getHeight(pos.add(j2, 0, k2)).getY() + 32);

                if (DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, new BlockPos(pos.getX() + j2, i1, pos.getZ() + k2))) {
                    break;
                }
            }
        }

        super.decorate(worldIn, rand, pos);
    }

    public int getGrassColorAtPos(BlockPos pos) {
        int i = super.getGrassColorAtPos(pos);
        return field_150632_aF == 3 ? (i & 16711422) + 2634762 >> 1 : i;
    }

    protected BiomeGenBase createMutatedBiome(final int p_180277_1_) {
        if (biomeID == BiomeGenBase.forest.biomeID) {
            BiomeGenForest biomegenforest = new BiomeGenForest(p_180277_1_, 1);
            biomegenforest.setHeight(new BiomeGenBase.Height(minHeight, maxHeight + 0.2F));
            biomegenforest.setBiomeName("Flower Forest");
            biomegenforest.func_150557_a(6976549, true);
            biomegenforest.setFillerBlockMetadata(8233509);
            return biomegenforest;
        } else {
            return biomeID != BiomeGenBase.birchForest.biomeID && biomeID != BiomeGenBase.birchForestHills.biomeID ? new BiomeGenMutated(p_180277_1_, this) {
                public void decorate(World worldIn, Random rand, BlockPos pos) {
                    baseBiome.decorate(worldIn, rand, pos);
                }
            } : new BiomeGenMutated(p_180277_1_, this) {
                public WorldGenAbstractTree genBigTreeChance(Random rand) {
                    return rand.nextBoolean() ? BiomeGenForest.field_150629_aC : BiomeGenForest.field_150630_aD;
                }
            };
        }
    }
}
