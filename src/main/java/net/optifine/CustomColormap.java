package net.optifine;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchBlock;
import net.optifine.config.Matches;
import net.optifine.util.TextureUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomColormap implements CustomColors.IColorizer {
    public static final String[] FORMAT_STRINGS = new String[]{"vanilla", "grid", "fixed"};
    public final String name;
    public final String basePath;
    private int format = -1;
    private MatchBlock[] matchBlocks = null;
    private String source = null;
    private int color = -1;
    private int yVariance = 0;
    private int yOffset = 0;
    private int width = 0;
    private int height = 0;
    private int[] colors = null;
    private float[][] colorsRgb = null;

    public CustomColormap(Properties props, String path, int width, int height, String formatDefault) {
        ConnectedParser connectedparser = new ConnectedParser("Colormap");
        name = connectedparser.parseName(path);
        basePath = connectedparser.parseBasePath(path);
        format = parseFormat(props.getProperty("format", formatDefault));
        matchBlocks = connectedparser.parseMatchBlocks(props.getProperty("blocks"));
        source = parseTexture(props.getProperty("source"), path, basePath);
        color = ConnectedParser.parseColor(props.getProperty("color"), -1);
        yVariance = connectedparser.parseInt(props.getProperty("yVariance"), 0);
        yOffset = connectedparser.parseInt(props.getProperty("yOffset"), 0);
        this.width = width;
        this.height = height;
    }

    private static void dbg(String str) {
        Config.dbg("CustomColors: " + str);
    }

    private static void warn(String str) {
        Config.warn("CustomColors: " + str);
    }

    private static String parseTexture(String texStr, String path, String basePath) {
        if (texStr != null) {
            texStr = texStr.trim();
            String s1 = ".png";

            if (texStr.endsWith(s1)) {
                texStr = texStr.substring(0, texStr.length() - s1.length());
            }

            texStr = fixTextureName(texStr, basePath);
            return texStr;
        } else {
            String s = path;
            int i = path.lastIndexOf(47);

            if (i >= 0) {
                s = path.substring(i + 1);
            }

            int j = s.lastIndexOf(46);

            if (j >= 0) {
                s = s.substring(0, j);
            }

            s = fixTextureName(s, basePath);
            return s;
        }
    }

    private static String fixTextureName(String iconName, String basePath) {
        iconName = TextureUtils.fixResourcePath(iconName, basePath);

        if (!iconName.startsWith(basePath) && !iconName.startsWith("textures/") && !iconName.startsWith("mcpatcher/")) {
            iconName = basePath + "/" + iconName;
        }

        if (iconName.endsWith(".png")) {
            iconName = iconName.substring(0, iconName.length() - 4);
        }

        String s = "textures/blocks/";

        if (iconName.startsWith(s)) {
            iconName = iconName.substring(s.length());
        }

        if (iconName.startsWith("/")) {
            iconName = iconName.substring(1);
        }

        return iconName;
    }

    private static float[][] toRgb(int[] cols) {
        float[][] afloat = new float[cols.length][3];

        for (int i = 0; i < cols.length; ++i) {
            int j = cols[i];
            float f = (float) (j >> 16 & 255) / 255.0F;
            float f1 = (float) (j >> 8 & 255) / 255.0F;
            float f2 = (float) (j & 255) / 255.0F;
            float[] afloat1 = afloat[i];
            afloat1[0] = f;
            afloat1[1] = f1;
            afloat1[2] = f2;
        }

        return afloat;
    }

    private int parseFormat(String str) {
        if (str == null) {
            return 0;
        } else {
            str = str.trim();

            return switch (str) {
                case "vanilla" -> 0;
                case "grid" -> 1;
                case "fixed" -> 2;
                default -> {
                    warn("Unknown format: " + str);
                    yield -1;
                }
            };
        }
    }

    public boolean isValid(String path) {
        if (format != 0 && format != 1) {
            if (format != 2) {
                return false;
            }

            if (color < 0) {
                color = 16777215;
            }
        } else {
            if (source == null) {
                warn("Source not defined: " + path);
                return false;
            }

            readColors();

            if (colors == null) {
                return false;
            }

            if (color < 0) {
                if (format == 0) {
                    color = getColor(127, 127);
                }

                if (format == 1) {
                    color = getColorGrid(BiomeGenBase.plains, new BlockPos(0, 64, 0));
                }
            }
        }

        return true;
    }

    public boolean isValidMatchBlocks(String path) {
        if (matchBlocks == null) {
            matchBlocks = detectMatchBlocks();

            if (matchBlocks == null) {
                warn("Match blocks not defined: " + path);
                return false;
            }
        }

        return true;
    }

    private MatchBlock[] detectMatchBlocks() {
        Block block = Block.getBlockFromName(name);

        if (block != null) {
            return new MatchBlock[]{new MatchBlock(Block.getIdFromBlock(block))};
        } else {
            Pattern pattern = Pattern.compile("^block([0-9]+).*$");
            Matcher matcher = pattern.matcher(name);

            if (matcher.matches()) {
                String s = matcher.group(1);
                int i = Config.parseInt(s, -1);

                if (i >= 0) {
                    return new MatchBlock[]{new MatchBlock(i)};
                }
            }

            ConnectedParser connectedparser = new ConnectedParser("Colormap");
            return connectedparser.parseMatchBlock(name);
        }
    }

    private void readColors() {
        try {
            colors = null;

            if (source == null) {
                return;
            }

            String s = source + ".png";
            ResourceLocation resourcelocation = new ResourceLocation(s);
            InputStream inputstream = Config.getResourceStream(resourcelocation);

            if (inputstream == null) {
                return;
            }

            BufferedImage bufferedimage = TextureUtil.readBufferedImage(inputstream);

            if (bufferedimage == null) {
                return;
            }

            int i = bufferedimage.getWidth();
            int j = bufferedimage.getHeight();
            boolean flag = width < 0 || width == i;
            boolean flag1 = height < 0 || height == j;

            if (!flag || !flag1) {
                dbg("Non-standard palette size: " + i + "x" + j + ", should be: " + width + "x" + height + ", path: " + s);
            }

            width = i;
            height = j;

            if (width <= 0 || height <= 0) {
                warn("Invalid palette size: " + i + "x" + j + ", path: " + s);
                return;
            }

            colors = new int[i * j];
            bufferedimage.getRGB(0, 0, i, j, colors, 0, i);
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    public boolean matchesBlock(BlockStateBase blockState) {
        return Matches.block(blockState, matchBlocks);
    }

    public int getColorRandom() {
        if (format == 2) {
            return color;
        } else {
            int i = CustomColors.random.nextInt(colors.length);
            return colors[i];
        }
    }

    public int getColor(int index) {
        index = Config.limit(index, 0, colors.length - 1);
        return colors[index] & 16777215;
    }

    public int getColor(int cx, int cy) {
        cx = Config.limit(cx, 0, width - 1);
        cy = Config.limit(cy, 0, height - 1);
        return colors[cy * width + cx] & 16777215;
    }

    public float[][] getColorsRgb() {
        if (colorsRgb == null) {
            colorsRgb = toRgb(colors);
        }

        return colorsRgb;
    }

    public int getColor(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos) {
        return getColor(blockAccess, blockPos);
    }

    public int getColor(IBlockAccess blockAccess, BlockPos blockPos) {
        BiomeGenBase biomegenbase = CustomColors.getColorBiome(blockAccess, blockPos);
        return getColor(biomegenbase, blockPos);
    }

    public boolean isColorConstant() {
        return format == 2;
    }

    public int getColor(BiomeGenBase biome, BlockPos blockPos) {
        return format == 0 ? getColorVanilla(biome, blockPos) : (format == 1 ? getColorGrid(biome, blockPos) : color);
    }

    public int getColorSmooth(IBlockAccess blockAccess, double x, double y, double z, int radius) {
        if (format == 2) {
            return color;
        } else {
            int i = MathHelper.floor_double(x);
            int j = MathHelper.floor_double(y);
            int k = MathHelper.floor_double(z);
            int l = 0;
            int i1 = 0;
            int j1 = 0;
            int k1 = 0;
            BlockPosM blockposm = new BlockPosM(0, 0, 0);

            for (int l1 = i - radius; l1 <= i + radius; ++l1) {
                for (int i2 = k - radius; i2 <= k + radius; ++i2) {
                    blockposm.setXyz(l1, j, i2);
                    int j2 = getColor(blockAccess, blockposm);
                    l += j2 >> 16 & 255;
                    i1 += j2 >> 8 & 255;
                    j1 += j2 & 255;
                    ++k1;
                }
            }

            int k2 = l / k1;
            int l2 = i1 / k1;
            int i3 = j1 / k1;
            return k2 << 16 | l2 << 8 | i3;
        }
    }

    private int getColorVanilla(BiomeGenBase biome, BlockPos blockPos) {
        double d0 = MathHelper.clamp_float(biome.getFloatTemperature(blockPos), 0.0F, 1.0F);
        double d1 = MathHelper.clamp_float(biome.getFloatRainfall(), 0.0F, 1.0F);
        d1 = d1 * d0;
        int i = (int) ((1.0D - d0) * (double) (width - 1));
        int j = (int) ((1.0D - d1) * (double) (height - 1));
        return getColor(i, j);
    }

    private int getColorGrid(BiomeGenBase biome, BlockPos blockPos) {
        int i = biome.biomeID;
        int j = blockPos.getY() - yOffset;

        if (yVariance > 0) {
            int k = blockPos.getX() << 16 + blockPos.getZ();
            int l = Config.intHash(k);
            int i1 = yVariance * 2 + 1;
            int j1 = (l & 255) % i1 - yVariance;
            j += j1;
        }

        return getColor(i, j);
    }

    public int getLength() {
        return format == 2 ? 1 : colors.length;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void addMatchBlock(MatchBlock mb) {
        if (matchBlocks == null) {
            matchBlocks = new MatchBlock[0];
        }

        matchBlocks = (MatchBlock[]) Config.addObjectToArray(matchBlocks, mb);
    }

    private MatchBlock getMatchBlock(int blockId) {
        if (matchBlocks != null) {
            for (MatchBlock matchblock : matchBlocks) {
                if (matchblock.getBlockId() == blockId) {
                    return matchblock;
                }
            }

        }
        return null;
    }

    public int[] getMatchBlockIds() {
        if (matchBlocks == null) {
            return null;
        } else {
            Set set = new HashSet<>();

            for (MatchBlock matchblock : matchBlocks) {
                if (matchblock.getBlockId() >= 0) {
                    set.add(matchblock.getBlockId());
                }
            }

            Integer[] ainteger = (Integer[]) set.toArray(new Integer[0]);
            int[] aint = new int[ainteger.length];

            for (int j = 0; j < ainteger.length; ++j) {
                aint[j] = ainteger[j];
            }

            return aint;
        }
    }

    public String toString() {
        return basePath + "/" + name + ", blocks: " + Config.arrayToString(matchBlocks) + ", source: " + source;
    }
}
