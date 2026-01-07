package net.optifine;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.config.*;
import net.optifine.util.ArrayUtils;
import net.optifine.util.MathUtils;

import java.util.Properties;

public class RandomEntityRule {
    public int[] sumWeights = null;
    public int sumAllWeights = 1;
    private final String pathProps;
    private final ResourceLocation baseResLoc;
    private final int index;
    private final int[] textures;
    private ResourceLocation[] resourceLocations = null;
    private int[] weights;
    private final BiomeGenBase[] biomes;
    private RangeListInt heights;
    private RangeListInt healthRange = null;
    private boolean healthPercent = false;
    private NbtTagValue nbtName = null;
    private VillagerProfession[] professions = null;
    private EnumDyeColor[] collarColors = null;
    private Boolean baby = null;
    private RangeListInt moonPhases = null;
    private RangeListInt dayTimes = null;
    private Weather[] weatherList = null;

    public RandomEntityRule(Properties props, String pathProps, ResourceLocation baseResLoc, int index,
                            String valTextures, ConnectedParser cp) {
        this.pathProps = pathProps;
        this.baseResLoc = baseResLoc;
        this.index = index;
        textures = cp.parseIntList(valTextures);
        weights = cp.parseIntList(props.getProperty("weights." + index));
        biomes = cp.parseBiomes(props.getProperty("biomes." + index));
        heights = cp.parseRangeListInt(props.getProperty("heights." + index));

        if (heights == null) {
            heights = parseMinMaxHeight(props, index);
        }

        String s = props.getProperty("health." + index);

        if (s != null) {
            healthPercent = s.contains("%");
            s = s.replace("%", "");
            healthRange = cp.parseRangeListInt(s);
        }

        nbtName = cp.parseNbtTagValue("name", props.getProperty("name." + index));
        professions = cp.parseProfessions(props.getProperty("professions." + index));
        collarColors = cp.parseDyeColors(props.getProperty("collarColors." + index), "collar color",
                ConnectedParser.DYE_COLORS_INVALID);
        baby = cp.parseBooleanObject(props.getProperty("baby." + index));
        moonPhases = cp.parseRangeListInt(props.getProperty("moonPhase." + index));
        dayTimes = cp.parseRangeListInt(props.getProperty("dayTime." + index));
        weatherList = cp.parseWeather(props.getProperty("weather." + index), "weather." + index, null);
    }

    private RangeListInt parseMinMaxHeight(Properties props, int index) {
        String s = props.getProperty("minHeight." + index);
        String s1 = props.getProperty("maxHeight." + index);

        if (s == null && s1 == null) {
            return null;
        } else {
            int i = 0;

            if (s != null) {
                i = Config.parseInt(s, -1);

                if (i < 0) {
                    Config.warn("Invalid minHeight: " + s);
                    return null;
                }
            }

            int j = 256;

            if (s1 != null) {
                j = Config.parseInt(s1, -1);

                if (j < 0) {
                    Config.warn("Invalid maxHeight: " + s1);
                    return null;
                }
            }

            RangeListInt rangelistint = new RangeListInt();
            rangelistint.addRange(new RangeInt(i, j));
            return rangelistint;
        }
    }

    public boolean isValid(String path) {
        if (textures != null && textures.length != 0) {
            if (resourceLocations != null) {
                return true;
            } else {
                resourceLocations = new ResourceLocation[textures.length];
                boolean flag = pathProps.startsWith("mcpatcher/mob/");
                ResourceLocation resourcelocation = RandomEntities.getLocationRandom(baseResLoc, flag);

                if (resourcelocation == null) {
                    Config.warn("Invalid path: " + baseResLoc.getResourcePath());
                    return false;
                } else {
                    for (int i = 0; i < resourceLocations.length; ++i) {
                        int j = textures[i];

                        if (j <= 1) {
                            resourceLocations[i] = baseResLoc;
                        } else {
                            ResourceLocation resourcelocation1 = RandomEntities.getLocationIndexed(resourcelocation, j);

                            if (resourcelocation1 == null) {
                                Config.warn("Invalid path: " + baseResLoc.getResourcePath());
                                return false;
                            }

                            if (!Config.hasResource(resourcelocation1)) {
                                Config.warn("Texture not found: " + resourcelocation1.getResourcePath());
                                return false;
                            }

                            resourceLocations[i] = resourcelocation1;
                        }
                    }

                    if (weights != null) {
                        if (weights.length > resourceLocations.length) {
                            Config.warn("More weights defined than skins, trimming weights: " + path);
                            int[] aint = new int[resourceLocations.length];
                            System.arraycopy(weights, 0, aint, 0, aint.length);
                            weights = aint;
                        }

                        if (weights.length < resourceLocations.length) {
                            Config.warn("Less weights defined than skins, expanding weights: " + path);
                            int[] aint1 = new int[resourceLocations.length];
                            System.arraycopy(weights, 0, aint1, 0, weights.length);
                            int l = MathUtils.getAverage(weights);

                            for (int j1 = weights.length; j1 < aint1.length; ++j1) {
                                aint1[j1] = l;
                            }

                            weights = aint1;
                        }

                        sumWeights = new int[weights.length];
                        int k = 0;

                        for (int i1 = 0; i1 < weights.length; ++i1) {
                            if (weights[i1] < 0) {
                                Config.warn("Invalid weight: " + weights[i1]);
                                return false;
                            }

                            k += weights[i1];
                            sumWeights[i1] = k;
                        }

                        sumAllWeights = k;

                        if (sumAllWeights <= 0) {
                            Config.warn("Invalid sum of all weights: " + k);
                            sumAllWeights = 1;
                        }
                    }

                    if (professions == ConnectedParser.PROFESSIONS_INVALID) {
                        Config.warn("Invalid professions or careers: " + path);
                        return false;
                    } else if (collarColors == ConnectedParser.DYE_COLORS_INVALID) {
                        Config.warn("Invalid collar colors: " + path);
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        } else {
            Config.warn("Invalid skins for rule: " + index);
            return false;
        }
    }

    public boolean matches(IRandomEntity randomEntity) {
        if (biomes != null && !Matches.biome(randomEntity.getSpawnBiome(), biomes)) {
            return false;
        } else {
            if (heights != null) {
                BlockPos blockpos = randomEntity.getSpawnPosition();

                if (blockpos != null && !heights.isInRange(blockpos.getY())) {
                    return false;
                }
            }

            if (healthRange != null) {
                int i1 = randomEntity.getHealth();

                if (healthPercent) {
                    int i = randomEntity.getMaxHealth();

                    if (i > 0) {
                        i1 = (int) ((double) (i1 * 100) / (double) i);
                    }
                }

                if (!healthRange.isInRange(i1)) {
                    return false;
                }
            }

            if (nbtName != null) {
                String s = randomEntity.getName();

                if (!nbtName.matchesValue(s)) {
                    return false;
                }
            }

            if (professions != null && randomEntity instanceof RandomEntity randomentity) {
                Entity entity = randomentity.getEntity();

                if (entity instanceof EntityVillager entityvillager) {
                    int j = entityvillager.getProfession();
                    int k = entityvillager.careerId;

                    if (j < 0 || k < 0) {
                        return false;
                    }

                    boolean flag = false;

                    for (VillagerProfession villagerprofession : professions) {
                        if (villagerprofession.matches(j, k)) {
                            flag = true;
                            break;
                        }
                    }

                    if (!flag) {
                        return false;
                    }
                }
            }

            if (collarColors != null && randomEntity instanceof RandomEntity randomentity1) {
                Entity entity1 = randomentity1.getEntity();

                if (entity1 instanceof EntityWolf entitywolf) {

                    if (!entitywolf.isTamed()) {
                        return false;
                    }

                    EnumDyeColor enumdyecolor = entitywolf.getCollarColor();

                    if (!Config.equalsOne(enumdyecolor, collarColors)) {
                        return false;
                    }
                }
            }

            if (baby != null && randomEntity instanceof RandomEntity randomentity2) {
                Entity entity2 = randomentity2.getEntity();

                if (entity2 instanceof EntityLiving entityliving) {

                    if (entityliving.isChild() != baby) {
                        return false;
                    }
                }
            }

            if (moonPhases != null) {
                World world = Config.getMinecraft().theWorld;

                if (world != null) {
                    int j1 = world.getMoonPhase();

                    if (!moonPhases.isInRange(j1)) {
                        return false;
                    }
                }
            }

            if (dayTimes != null) {
                World world1 = Config.getMinecraft().theWorld;

                if (world1 != null) {
                    int k1 = (int) world1.getWorldInfo().getWorldTime();

                    if (!dayTimes.isInRange(k1)) {
                        return false;
                    }
                }
            }

            if (weatherList != null) {
                World world2 = Config.getMinecraft().theWorld;

                if (world2 != null) {
                    Weather weather = Weather.getWeather(world2, 0.0F);

                    return ArrayUtils.contains(weatherList, weather);
                }
            }

            return true;
        }
    }

    public ResourceLocation getTextureLocation(ResourceLocation loc, int randomId) {
        if (resourceLocations != null && resourceLocations.length != 0) {
            int i = 0;

            if (weights == null) {
                i = randomId % resourceLocations.length;
            } else {
                int j = randomId % sumAllWeights;

                for (int k = 0; k < sumWeights.length; ++k) {
                    if (sumWeights[k] > j) {
                        i = k;
                        break;
                    }
                }
            }

            return resourceLocations[i];
        } else {
            return loc;
        }
    }
}
