package net.optifine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.config.ConnectedParser;
import net.optifine.config.Matches;
import net.optifine.config.RangeListInt;
import net.optifine.render.Blender;
import net.optifine.util.NumUtils;
import net.optifine.util.SmoothFloat;
import net.optifine.util.TextureUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class CustomSkyLayer {
    public static final float[] DEFAULT_AXIS = new float[]{1.0F, 0.0F, 0.0F};
    public String source;
    public BiomeGenBase[] biomes;
    public RangeListInt heights;
    public int textureId;
    private int startFadeIn = -1;
    private int endFadeIn = -1;
    private int startFadeOut = -1;
    private int endFadeOut = -1;
    private int blend = 1;
    private boolean rotate = false;
    private float speed = 1.0F;
    private float[] axis;
    private RangeListInt days;
    private int daysLoop;
    private boolean weatherClear;
    private boolean weatherRain;
    private boolean weatherThunder;
    private float transition;
    private SmoothFloat smoothPositionBrightness;
    private World lastWorld;

    public CustomSkyLayer(Properties props, String defSource) {
        axis = DEFAULT_AXIS;
        days = null;
        daysLoop = 8;
        weatherClear = true;
        weatherRain = false;
        weatherThunder = false;
        biomes = null;
        heights = null;
        transition = 1.0F;
        smoothPositionBrightness = null;
        textureId = -1;
        lastWorld = null;
        ConnectedParser connectedparser = new ConnectedParser("CustomSky");
        source = props.getProperty("source", defSource);
        startFadeIn = parseTime(props.getProperty("startFadeIn"));
        endFadeIn = parseTime(props.getProperty("endFadeIn"));
        startFadeOut = parseTime(props.getProperty("startFadeOut"));
        endFadeOut = parseTime(props.getProperty("endFadeOut"));
        blend = Blender.parseBlend(props.getProperty("blend"));
        rotate = parseBoolean(props.getProperty("rotate"), true);
        speed = parseFloat(props.getProperty("speed"), 1.0F);
        axis = parseAxis(props.getProperty("axis"), DEFAULT_AXIS);
        days = connectedparser.parseRangeListInt(props.getProperty("days"));
        daysLoop = connectedparser.parseInt(props.getProperty("daysLoop"), 8);
        List<String> list = parseWeatherList(props.getProperty("weather", "clear"));
        weatherClear = list.contains("clear");
        weatherRain = list.contains("rain");
        weatherThunder = list.contains("thunder");
        biomes = connectedparser.parseBiomes(props.getProperty("biomes"));
        heights = connectedparser.parseRangeListInt(props.getProperty("heights"));
        transition = parseFloat(props.getProperty("transition"), 1.0F);
    }

    private List<String> parseWeatherList(String str) {
        List<String> list = Arrays.asList("clear", "rain", "thunder");
        List<String> list1 = new ArrayList<>();
        String[] astring = Config.tokenize(str, " ");

        for (String s : astring) {
            if (!list.contains(s)) {
                Config.warn("Unknown weather: " + s);
            } else {
                list1.add(s);
            }
        }

        return list1;
    }

    private int parseTime(String str) {
        if (str == null) {
            return -1;
        } else {
            String[] astring = Config.tokenize(str, ":");

            if (astring.length != 2) {
                Config.warn("Invalid time: " + str);
                return -1;
            } else {
                String s = astring[0];
                String s1 = astring[1];
                int i = Config.parseInt(s, -1);
                int j = Config.parseInt(s1, -1);

                if (i >= 0 && i <= 23 && j >= 0 && j <= 59) {
                    i = i - 6;

                    if (i < 0) {
                        i += 24;
                    }

                    return i * 1000 + (int) ((double) j / 60.0D * 1000.0D);
                } else {
                    Config.warn("Invalid time: " + str);
                    return -1;
                }
            }
        }
    }

    private boolean parseBoolean(String str, boolean defVal) {
        if (str == null) {
            return defVal;
        } else if (str.equalsIgnoreCase("true")) {
            return true;
        } else if (str.equalsIgnoreCase("false")) {
            return false;
        } else {
            Config.warn("Unknown boolean: " + str);
            return defVal;
        }
    }

    private float parseFloat(String str, float defVal) {
        if (str == null) {
            return defVal;
        } else {
            float f = Config.parseFloat(str, Float.MIN_VALUE);

            if (f == Float.MIN_VALUE) {
                Config.warn("Invalid value: " + str);
                return defVal;
            } else {
                return f;
            }
        }
    }

    private float[] parseAxis(String str, float[] defVal) {
        if (str == null) {
            return defVal;
        } else {
            String[] astring = Config.tokenize(str, " ");

            if (astring.length != 3) {
                Config.warn("Invalid axis: " + str);
                return defVal;
            } else {
                float[] afloat = new float[3];

                for (int i = 0; i < astring.length; ++i) {
                    afloat[i] = Config.parseFloat(astring[i], Float.MIN_VALUE);

                    if (afloat[i] == Float.MIN_VALUE) {
                        Config.warn("Invalid axis: " + str);
                        return defVal;
                    }

                    if (afloat[i] < -1.0F || afloat[i] > 1.0F) {
                        Config.warn("Invalid axis values: " + str);
                        return defVal;
                    }
                }

                float f2 = afloat[0];
                float f = afloat[1];
                float f1 = afloat[2];

                if (f2 * f2 + f * f + f1 * f1 < 1.0E-5F) {
                    Config.warn("Invalid axis values: " + str);
                    return defVal;
                } else {
                    return new float[]{f1, f, -f2};
                }
            }
        }
    }

    public boolean isValid(String path) {
        if (source == null) {
            Config.warn("No source texture: " + path);
            return false;
        } else {
            source = TextureUtils.fixResourcePath(source, TextureUtils.getBasePath(path));

            if (startFadeIn >= 0 && endFadeIn >= 0 && endFadeOut >= 0) {
                int i = normalizeTime(endFadeIn - startFadeIn);

                if (startFadeOut < 0) {
                    startFadeOut = normalizeTime(endFadeOut - i);

                    if (timeBetween(startFadeOut, startFadeIn, endFadeIn)) {
                        startFadeOut = endFadeIn;
                    }
                }

                int j = normalizeTime(startFadeOut - endFadeIn);
                int k = normalizeTime(endFadeOut - startFadeOut);
                int l = normalizeTime(startFadeIn - endFadeOut);
                int i1 = i + j + k + l;

                if (i1 != 24000) {
                    Config.warn("Invalid fadeIn/fadeOut times, sum is not 24h: " + i1);
                    return false;
                } else if (speed < 0.0F) {
                    Config.warn("Invalid speed: " + speed);
                    return false;
                } else if (daysLoop <= 0) {
                    Config.warn("Invalid daysLoop: " + daysLoop);
                    return false;
                } else {
                    return true;
                }
            } else {
                Config.warn("Invalid times, required are: startFadeIn, endFadeIn and endFadeOut.");
                return false;
            }
        }
    }

    private int normalizeTime(int timeMc) {
        while (timeMc >= 24000) {
            timeMc -= 24000;
        }

        while (timeMc < 0) {
            timeMc += 24000;
        }

        return timeMc;
    }

    public void render(World world, int timeOfDay, float celestialAngle, float rainStrength, float thunderStrength) {
        float f = getPositionBrightness(world);
        float f1 = getWeatherBrightness(rainStrength, thunderStrength);
        float f2 = getFadeBrightness(timeOfDay);
        float f3 = f * f1 * f2;
        f3 = Config.limit(f3, 0.0F, 1.0F);

        if (f3 >= 1.0E-4F) {
            GlStateManager.bindTexture(textureId);
            Blender.setupBlend(blend, f3);
            GlStateManager.pushMatrix();

            if (rotate) {
                float f4 = 0.0F;

                if (speed != (float) Math.round(speed)) {
                    long i = (world.getWorldTime() + 18000L) / 24000L;
                    double d0 = speed % 1.0F;
                    double d1 = (double) i * d0;
                    f4 = (float) (d1 % 1.0D);
                }

                GlStateManager.rotate(360.0F * (f4 + celestialAngle * speed), axis[0], axis[1], axis[2]);
            }

            Tessellator tessellator = Tessellator.getInstance();
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
            renderSide(tessellator, 4);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            renderSide(tessellator, 1);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            renderSide(tessellator, 0);
            GlStateManager.popMatrix();
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            renderSide(tessellator, 5);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            renderSide(tessellator, 2);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            renderSide(tessellator, 3);
            GlStateManager.popMatrix();
        }
    }

    private float getPositionBrightness(World world) {
        if (biomes == null && heights == null) {
            return 1.0F;
        } else {
            float f = getPositionBrightnessRaw(world);

            if (smoothPositionBrightness == null) {
                smoothPositionBrightness = new SmoothFloat(f, transition);
            }

            f = smoothPositionBrightness.getSmoothValue(f);
            return f;
        }
    }

    private float getPositionBrightnessRaw(World world) {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();

        if (entity == null) {
            return 0.0F;
        } else {
            BlockPos blockpos = entity.getPosition();

            if (biomes != null) {
                BiomeGenBase biomegenbase = world.getBiomeGenForCoords(blockpos);

                if (biomegenbase == null) {
                    return 0.0F;
                }

                if (!Matches.biome(biomegenbase, biomes)) {
                    return 0.0F;
                }
            }

            return heights != null && !heights.isInRange(blockpos.getY()) ? 0.0F : 1.0F;
        }
    }

    private float getWeatherBrightness(float rainStrength, float thunderStrength) {
        float f = 1.0F - rainStrength;
        float f1 = rainStrength - thunderStrength;
        float f2 = 0.0F;

        if (weatherClear) {
            f2 += f;
        }

        if (weatherRain) {
            f2 += f1;
        }

        if (weatherThunder) {
            f2 += thunderStrength;
        }

        f2 = NumUtils.limit(f2, 0.0F, 1.0F);
        return f2;
    }

    private float getFadeBrightness(int timeOfDay) {
        if (timeBetween(timeOfDay, startFadeIn, endFadeIn)) {
            int k = normalizeTime(endFadeIn - startFadeIn);
            int l = normalizeTime(timeOfDay - startFadeIn);
            return (float) l / (float) k;
        } else if (timeBetween(timeOfDay, endFadeIn, startFadeOut)) {
            return 1.0F;
        } else if (timeBetween(timeOfDay, startFadeOut, endFadeOut)) {
            int i = normalizeTime(endFadeOut - startFadeOut);
            int j = normalizeTime(timeOfDay - startFadeOut);
            return 1.0F - (float) j / (float) i;
        } else {
            return 0.0F;
        }
    }

    private void renderSide(Tessellator tess, int side) {
        WorldRenderer worldrenderer = tess.getWorldRenderer();
        double d0 = (double) (side % 3) / 3.0D;
        double d1 = (double) (side / 3) / 2.0D;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-100.0D, -100.0D, -100.0D).tex(d0, d1).endVertex();
        worldrenderer.pos(-100.0D, -100.0D, 100.0D).tex(d0, d1 + 0.5D).endVertex();
        worldrenderer.pos(100.0D, -100.0D, 100.0D).tex(d0 + 0.3333333333333333D, d1 + 0.5D).endVertex();
        worldrenderer.pos(100.0D, -100.0D, -100.0D).tex(d0 + 0.3333333333333333D, d1).endVertex();
        tess.draw();
    }

    public boolean isActive(World world, int timeOfDay) {
        if (world != lastWorld) {
            lastWorld = world;
            smoothPositionBrightness = null;
        }

        if (timeBetween(timeOfDay, endFadeOut, startFadeIn)) {
            return false;
        } else {
            if (days != null) {
                long i = world.getWorldTime();
                long j;

                for (j = i - (long) startFadeIn; j < 0L; j += 24000L * daysLoop) {
                }

                int k = (int) (j / 24000L);
                int l = k % daysLoop;

                return days.isInRange(l);
            }

            return true;
        }
    }

    private boolean timeBetween(int timeOfDay, int timeStart, int timeEnd) {
        return timeStart <= timeEnd ? timeOfDay >= timeStart && timeOfDay <= timeEnd : timeOfDay >= timeStart || timeOfDay <= timeEnd;
    }

    public String toString() {
        return source + ", " + startFadeIn + "-" + endFadeIn + " " + startFadeOut + "-" + endFadeOut;
    }
}
