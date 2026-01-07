package net.optifine;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.src.Config;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.config.*;
import net.optifine.util.MathUtils;
import net.optifine.util.TextureUtils;

import java.util.*;

public class ConnectedProperties {
    public final String name;
    public final String basePath;
    public MatchBlock[] matchBlocks;
    public final int[] metadatas;
    public String[] matchTiles = null;
    public int method = 0;
    public String[] tiles = null;
    public int connect = 0;
    public int faces = 63;
    public BiomeGenBase[] biomes = null;
    public RangeListInt heights = null;
    public int renderPass = 0;
    public boolean innerSeams = false;
    public int[] ctmTileIndexes = null;
    public int width = 0;
    public int height = 0;
    public int[] weights = null;
    public int randomLoops = 0;
    public int symmetry = 1;
    public boolean linked = false;
    public NbtTagValue nbtName = null;
    public int[] sumWeights = null;
    public int sumAllWeights = 1;
    public TextureAtlasSprite[] matchTileIcons = null;
    public TextureAtlasSprite[] tileIcons = null;
    public MatchBlock[] connectBlocks = null;
    public String[] connectTiles = null;
    public TextureAtlasSprite[] connectTileIcons = null;
    public int tintIndex = -1;
    public IBlockState tintBlockState = Blocks.air.getDefaultState();
    public EnumWorldBlockLayer layer = null;

    public ConnectedProperties(Properties props, String path) {
        ConnectedParser connectedparser = new ConnectedParser("ConnectedTextures");
        name = connectedparser.parseName(path);
        basePath = connectedparser.parseBasePath(path);
        matchBlocks = connectedparser.parseMatchBlocks(props.getProperty("matchBlocks"));
        metadatas = connectedparser.parseIntList(props.getProperty("metadata"));
        matchTiles = parseMatchTiles(props.getProperty("matchTiles"));
        method = parseMethod(props.getProperty("method"));
        tiles = parseTileNames(props.getProperty("tiles"));
        connect = parseConnect(props.getProperty("connect"));
        faces = parseFaces(props.getProperty("faces"));
        biomes = connectedparser.parseBiomes(props.getProperty("biomes"));
        heights = connectedparser.parseRangeListInt(props.getProperty("heights"));

        if (heights == null) {
            int i = connectedparser.parseInt(props.getProperty("minHeight"), -1);
            int j = connectedparser.parseInt(props.getProperty("maxHeight"), 1024);

            if (i != -1 || j != 1024) {
                heights = new RangeListInt(new RangeInt(i, j));
            }
        }

        renderPass = connectedparser.parseInt(props.getProperty("renderPass"), -1);
        innerSeams = connectedparser.parseBoolean(props.getProperty("innerSeams"), false);
        ctmTileIndexes = parseCtmTileIndexes(props);
        width = connectedparser.parseInt(props.getProperty("width"), -1);
        height = connectedparser.parseInt(props.getProperty("height"), -1);
        weights = connectedparser.parseIntList(props.getProperty("weights"));
        randomLoops = connectedparser.parseInt(props.getProperty("randomLoops"), 0);
        symmetry = parseSymmetry(props.getProperty("symmetry"));
        linked = connectedparser.parseBoolean(props.getProperty("linked"), false);
        nbtName = connectedparser.parseNbtTagValue("name", props.getProperty("name"));
        connectBlocks = connectedparser.parseMatchBlocks(props.getProperty("connectBlocks"));
        connectTiles = parseMatchTiles(props.getProperty("connectTiles"));
        tintIndex = connectedparser.parseInt(props.getProperty("tintIndex"), -1);
        tintBlockState = connectedparser.parseBlockState(props.getProperty("tintBlock"), Blocks.air.getDefaultState());
        layer = connectedparser.parseBlockRenderLayer(props.getProperty("layer"), EnumWorldBlockLayer.CUTOUT_MIPPED);
    }

    private static int parseSymmetry(String str) {
        if (str == null) {
            return 1;
        } else {
            str = str.trim();

            if (str.equals("opposite")) {
                return 2;
            } else if (str.equals("all")) {
                return 6;
            } else {
                Config.warn("Unknown symmetry: " + str);
                return 1;
            }
        }
    }

    private static int parseFaces(String str) {
        if (str == null) {
            return 63;
        } else {
            String[] astring = Config.tokenize(str, " ,");
            int i = 0;

            for (String s : astring) {
                int k = parseFace(s);
                i |= k;
            }

            return i;
        }
    }

    private static int parseFace(String str) {
        str = str.toLowerCase();

        if (!str.equals("bottom") && !str.equals("down")) {
            if (!str.equals("top") && !str.equals("up")) {
                return switch (str) {
                    case "north" -> 4;
                    case "south" -> 8;
                    case "east" -> 32;
                    case "west" -> 16;
                    case "sides" -> 60;
                    case "all" -> 63;
                    default -> {
                        Config.warn("Unknown face: " + str);
                        yield 128;
                    }
                };
            } else {
                return 2;
            }
        } else {
            return 1;
        }
    }

    private static int parseConnect(String str) {
        if (str == null) {
            return 0;
        } else {
            str = str.trim();

            return switch (str) {
                case "block" -> 1;
                case "tile" -> 2;
                case "material" -> 3;
                default -> {
                    Config.warn("Unknown connect: " + str);
                    yield 128;
                }
            };
        }
    }

    public static IProperty getProperty(String key, Collection properties) {
        for (Object o : properties) {
            IProperty iproperty = (IProperty) o;
            if (key.equals(iproperty.getName())) {
                return iproperty;
            }
        }

        return null;
    }

    private static int parseMethod(String str) {
        if (str == null) {
            return 1;
        } else {
            str = str.trim();

            if (!str.equals("ctm") && !str.equals("glass")) {
                if (str.equals("ctm_compact")) {
                    return 10;
                } else if (!str.equals("horizontal") && !str.equals("bookshelf")) {
                    if (str.equals("vertical")) {
                        return 6;
                    } else if (str.equals("top")) {
                        return 3;
                    } else if (str.equals("random")) {
                        return 4;
                    } else if (str.equals("repeat")) {
                        return 5;
                    } else if (str.equals("fixed")) {
                        return 7;
                    } else if (!str.equals("horizontal+vertical") && !str.equals("h+v")) {
                        if (!str.equals("vertical+horizontal") && !str.equals("v+h")) {
                            return switch (str) {
                                case "overlay" -> 11;
                                case "overlay_fixed" -> 12;
                                case "overlay_random" -> 13;
                                case "overlay_repeat" -> 14;
                                case "overlay_ctm" -> 15;
                                default -> {
                                    Config.warn("Unknown method: " + str);
                                    yield 0;
                                }
                            };
                        } else {
                            return 9;
                        }
                    } else {
                        return 8;
                    }
                } else {
                    return 2;
                }
            } else {
                return 1;
            }
        }
    }

    private static TextureAtlasSprite getIcon(String iconName) {
        TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite textureatlassprite = texturemap.getSpriteSafe(iconName);

        if (textureatlassprite == null) {
            textureatlassprite = texturemap.getSpriteSafe("blocks/" + iconName);
        }
        return textureatlassprite;
    }

    private static boolean isMethodOverlay(int method) {
        return switch (method) {
            case 11, 12, 13, 14, 15 -> true;
            default -> false;
        };
    }

    private static TextureAtlasSprite[] registerIcons(String[] tileNames, TextureMap textureMap, boolean skipTiles, boolean defaultTiles) {
        if (tileNames == null) {
            return null;
        } else {
            List list = new ArrayList<>();

            for (String s : tileNames) {
                ResourceLocation resourcelocation = new ResourceLocation(s);
                String s1 = resourcelocation.getResourceDomain();
                String s2 = resourcelocation.getResourcePath();

                if (!s2.contains("/")) {
                    s2 = "textures/blocks/" + s2;
                }

                String s3 = s2 + ".png";

                if (skipTiles && s3.endsWith("<skip>.png")) {
                    list.add(null);
                } else if (defaultTiles && s3.endsWith("<default>.png")) {
                    list.add(ConnectedTextures.SPRITE_DEFAULT);
                } else {
                    ResourceLocation resourcelocation1 = new ResourceLocation(s1, s3);
                    boolean flag = Config.hasResource(resourcelocation1);

                    if (!flag) {
                        Config.warn("File not found: " + s3);
                    }

                    String s4 = "textures/";
                    String s5 = s2;

                    if (s2.startsWith(s4)) {
                        s5 = s2.substring(s4.length());
                    }

                    ResourceLocation resourcelocation2 = new ResourceLocation(s1, s5);
                    TextureAtlasSprite textureatlassprite = textureMap.registerSprite(resourcelocation2);
                    list.add(textureatlassprite);
                }
            }

            return (TextureAtlasSprite[]) list.toArray(new TextureAtlasSprite[0]);
        }
    }

    private int[] parseCtmTileIndexes(Properties props) {
        if (tiles == null) {
            return null;
        } else {
            Map<Integer, Integer> map = new HashMap<>();

            for (Object object : props.keySet()) {
                if (object instanceof String s) {
                    String s1 = "ctm.";

                    if (s.startsWith(s1)) {
                        String s2 = s.substring(s1.length());
                        String s3 = props.getProperty(s);

                        if (s3 != null) {
                            s3 = s3.trim();
                            int i = Config.parseInt(s2, -1);

                            if (i >= 0 && i <= 46) {
                                int j = Config.parseInt(s3, -1);

                                if (j >= 0 && j < tiles.length) {
                                    map.put(i, j);
                                } else {
                                    Config.warn("Invalid CTM tile index: " + s3);
                                }
                            } else {
                                Config.warn("Invalid CTM index: " + s2);
                            }
                        }
                    }
                }
            }

            if (map.isEmpty()) {
                return null;
            } else {
                int[] aint = new int[47];

                for (int k = 0; k < aint.length; ++k) {
                    aint[k] = -1;

                    if (map.containsKey(k)) {
                        aint[k] = map.get(k);
                    }
                }

                return aint;
            }
        }
    }

    private String[] parseMatchTiles(String str) {
        if (str == null) {
            return null;
        } else {
            String[] astring = Config.tokenize(str, " ");

            for (int i = 0; i < astring.length; ++i) {
                String s = astring[i];

                if (s.endsWith(".png")) {
                    s = s.substring(0, s.length() - 4);
                }

                s = TextureUtils.fixResourcePath(s, basePath);
                astring[i] = s;
            }

            return astring;
        }
    }

    private String[] parseTileNames(String str) {
        if (str == null) {
            return null;
        } else {
            List list = new ArrayList<>();
            String[] astring = Config.tokenize(str, " ,");
            label32:

            for (String s : astring) {
                if (s.contains("-")) {
                    String[] astring1 = Config.tokenize(s, "-");

                    if (astring1.length == 2) {
                        int j = Config.parseInt(astring1[0], -1);
                        int k = Config.parseInt(astring1[1], -1);

                        if (j >= 0 && k >= 0) {
                            if (j > k) {
                                Config.warn("Invalid interval: " + s + ", when parsing: " + str);
                                continue;
                            }

                            int l = j;

                            while (true) {
                                if (l > k) {
                                    continue label32;
                                }

                                list.add(String.valueOf(l));
                                ++l;
                            }
                        }
                    }
                }

                list.add(s);
            }

            String[] astring2 = (String[]) list.toArray(new String[0]);

            for (int i1 = 0; i1 < astring2.length; ++i1) {
                String s1 = astring2[i1];
                s1 = TextureUtils.fixResourcePath(s1, basePath);

                if (!s1.startsWith(basePath) && !s1.startsWith("textures/") && !s1.startsWith("mcpatcher/")) {
                    s1 = basePath + "/" + s1;
                }

                if (s1.endsWith(".png")) {
                    s1 = s1.substring(0, s1.length() - 4);
                }

                if (s1.startsWith("/")) {
                    s1 = s1.substring(1);
                }

                astring2[i1] = s1;
            }

            return astring2;
        }
    }

    public boolean isValid(String path) {
        if (name != null && !name.isEmpty()) {
            if (basePath == null) {
                Config.warn("No base path found: " + path);
                return false;
            } else {
                if (matchBlocks == null) {
                    matchBlocks = detectMatchBlocks();
                }

                if (matchTiles == null && matchBlocks == null) {
                    matchTiles = detectMatchTiles();
                }

                if (matchBlocks == null && matchTiles == null) {
                    Config.warn("No matchBlocks or matchTiles specified: " + path);
                    return false;
                } else if (method == 0) {
                    Config.warn("No method: " + path);
                    return false;
                } else if (tiles != null && tiles.length > 0) {
                    if (connect == 0) {
                        connect = detectConnect();
                    }

                    if (connect == 128) {
                        Config.warn("Invalid connect in: " + path);
                        return false;
                    } else if (renderPass > 0) {
                        Config.warn("Render pass not supported: " + renderPass);
                        return false;
                    } else if ((faces & 128) != 0) {
                        Config.warn("Invalid faces in: " + path);
                        return false;
                    } else if ((symmetry & 128) != 0) {
                        Config.warn("Invalid symmetry in: " + path);
                        return false;
                    } else {
                        return switch (method) {
                            case 1 -> isValidCtm(path);
                            case 2 -> isValidHorizontal(path);
                            case 3 -> isValidTop(path);
                            case 4 -> isValidRandom(path);
                            case 5 -> isValidRepeat(path);
                            case 6 -> isValidVertical(path);
                            case 7 -> isValidFixed(path);
                            case 8 -> isValidHorizontalVertical(path);
                            case 9 -> isValidVerticalHorizontal(path);
                            case 10 -> isValidCtmCompact(path);
                            case 11 -> isValidOverlay(path);
                            case 12 -> isValidOverlayFixed(path);
                            case 13 -> isValidOverlayRandom(path);
                            case 14 -> isValidOverlayRepeat(path);
                            case 15 -> isValidOverlayCtm(path);
                            default -> {
                                Config.warn("Unknown method: " + path);
                                yield false;
                            }
                        };
                    }
                } else {
                    Config.warn("No tiles specified: " + path);
                    return false;
                }
            }
        } else {
            Config.warn("No name found: " + path);
            return false;
        }
    }

    private int detectConnect() {
        return matchBlocks != null ? 1 : (matchTiles != null ? 2 : 128);
    }

    private MatchBlock[] detectMatchBlocks() {
        int[] aint = detectMatchBlockIds();

        if (aint == null) {
            return null;
        } else {
            MatchBlock[] amatchblock = new MatchBlock[aint.length];

            for (int i = 0; i < amatchblock.length; ++i) {
                amatchblock[i] = new MatchBlock(aint[i]);
            }

            return amatchblock;
        }
    }

    private int[] detectMatchBlockIds() {
        if (!name.startsWith("block")) {
            return null;
        } else {
            int i = "block".length();
            int j;

            for (j = i; j < name.length(); ++j) {
                char c0 = name.charAt(j);

                if (c0 < 48 || c0 > 57) {
                    break;
                }
            }

            if (j == i) {
                return null;
            } else {
                String s = name.substring(i, j);
                int k = Config.parseInt(s, -1);
                return k < 0 ? null : new int[]{k};
            }
        }
    }

    private String[] detectMatchTiles() {
        TextureAtlasSprite textureatlassprite = getIcon(name);
        return textureatlassprite == null ? null : new String[]{name};
    }

    private boolean isValidCtm(String path) {
        if (tiles == null) {
            tiles = parseTileNames("0-11 16-27 32-43 48-58");
        }

        if (tiles.length < 47) {
            Config.warn("Invalid tiles, must be at least 47: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidCtmCompact(String path) {
        if (tiles == null) {
            tiles = parseTileNames("0-4");
        }

        if (tiles.length < 5) {
            Config.warn("Invalid tiles, must be at least 5: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidOverlay(String path) {
        if (tiles == null) {
            tiles = parseTileNames("0-16");
        }

        if (tiles.length < 17) {
            Config.warn("Invalid tiles, must be at least 17: " + path);
            return false;
        } else if (layer != null && layer != EnumWorldBlockLayer.SOLID) {
            return true;
        } else {
            Config.warn("Invalid overlay layer: " + layer);
            return false;
        }
    }

    private boolean isValidOverlayFixed(String path) {
        if (!isValidFixed(path)) {
            return false;
        } else if (layer != null && layer != EnumWorldBlockLayer.SOLID) {
            return true;
        } else {
            Config.warn("Invalid overlay layer: " + layer);
            return false;
        }
    }

    private boolean isValidOverlayRandom(String path) {
        if (!isValidRandom(path)) {
            return false;
        } else if (layer != null && layer != EnumWorldBlockLayer.SOLID) {
            return true;
        } else {
            Config.warn("Invalid overlay layer: " + layer);
            return false;
        }
    }

    private boolean isValidOverlayRepeat(String path) {
        if (!isValidRepeat(path)) {
            return false;
        } else if (layer != null && layer != EnumWorldBlockLayer.SOLID) {
            return true;
        } else {
            Config.warn("Invalid overlay layer: " + layer);
            return false;
        }
    }

    private boolean isValidOverlayCtm(String path) {
        if (!isValidCtm(path)) {
            return false;
        } else if (layer != null && layer != EnumWorldBlockLayer.SOLID) {
            return true;
        } else {
            Config.warn("Invalid overlay layer: " + layer);
            return false;
        }
    }

    private boolean isValidHorizontal(String path) {
        if (tiles == null) {
            tiles = parseTileNames("12-15");
        }

        if (tiles.length != 4) {
            Config.warn("Invalid tiles, must be exactly 4: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidVertical(String path) {
        if (tiles == null) {
            Config.warn("No tiles defined for vertical: " + path);
            return false;
        } else if (tiles.length != 4) {
            Config.warn("Invalid tiles, must be exactly 4: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidHorizontalVertical(String path) {
        if (tiles == null) {
            Config.warn("No tiles defined for horizontal+vertical: " + path);
            return false;
        } else if (tiles.length != 7) {
            Config.warn("Invalid tiles, must be exactly 7: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidVerticalHorizontal(String path) {
        if (tiles == null) {
            Config.warn("No tiles defined for vertical+horizontal: " + path);
            return false;
        } else if (tiles.length != 7) {
            Config.warn("Invalid tiles, must be exactly 7: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidRandom(String path) {
        if (tiles != null && tiles.length > 0) {
            if (weights != null) {
                if (weights.length > tiles.length) {
                    Config.warn("More weights defined than tiles, trimming weights: " + path);
                    int[] aint = new int[tiles.length];
                    System.arraycopy(weights, 0, aint, 0, aint.length);
                    weights = aint;
                }

                if (weights.length < tiles.length) {
                    Config.warn("Less weights defined than tiles, expanding weights: " + path);
                    int[] aint1 = new int[tiles.length];
                    System.arraycopy(weights, 0, aint1, 0, weights.length);
                    int i = MathUtils.getAverage(weights);

                    for (int j = weights.length; j < aint1.length; ++j) {
                        aint1[j] = i;
                    }

                    weights = aint1;
                }

                sumWeights = new int[weights.length];
                int k = 0;

                for (int l = 0; l < weights.length; ++l) {
                    k += weights[l];
                    sumWeights[l] = k;
                }

                sumAllWeights = k;

                if (sumAllWeights <= 0) {
                    Config.warn("Invalid sum of all weights: " + k);
                    sumAllWeights = 1;
                }
            }

            if (randomLoops >= 0 && randomLoops <= 9) {
                return true;
            } else {
                Config.warn("Invalid randomLoops: " + randomLoops);
                return false;
            }
        } else {
            Config.warn("Tiles not defined: " + path);
            return false;
        }
    }

    private boolean isValidRepeat(String path) {
        if (tiles == null) {
            Config.warn("Tiles not defined: " + path);
            return false;
        } else if (width <= 0) {
            Config.warn("Invalid width: " + path);
            return false;
        } else if (height <= 0) {
            Config.warn("Invalid height: " + path);
            return false;
        } else if (tiles.length != width * height) {
            Config.warn("Number of tiles does not equal width x height: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidFixed(String path) {
        if (tiles == null) {
            Config.warn("Tiles not defined: " + path);
            return false;
        } else if (tiles.length != 1) {
            Config.warn("Number of tiles should be 1 for method: fixed.");
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidTop(String path) {
        if (tiles == null) {
            tiles = parseTileNames("66");
        }

        if (tiles.length != 1) {
            Config.warn("Invalid tiles, must be exactly 1: " + path);
            return false;
        } else {
            return true;
        }
    }

    public void updateIcons(TextureMap textureMap) {
        if (matchTiles != null) {
            matchTileIcons = registerIcons(matchTiles, textureMap, false, false);
        }

        if (connectTiles != null) {
            connectTileIcons = registerIcons(connectTiles, textureMap, false, false);
        }

        if (tiles != null) {
            tileIcons = registerIcons(tiles, textureMap, true, !isMethodOverlay(method));
        }
    }

    public boolean matchesBlockId(int blockId) {
        return Matches.blockId(blockId, matchBlocks);
    }

    public boolean matchesBlock(int blockId, int metadata) {
        return Matches.block(blockId, metadata, matchBlocks) && Matches.metadata(metadata, metadatas);
    }

    public boolean matchesIcon(TextureAtlasSprite icon) {
        return Matches.sprite(icon, matchTileIcons);
    }

    public String toString() {
        return "CTM name: " + name + ", basePath: " + basePath + ", matchBlocks: " + Config.arrayToString(matchBlocks) + ", matchTiles: " + Config.arrayToString(matchTiles);
    }

    public boolean matchesBiome(BiomeGenBase biome) {
        return Matches.biome(biome, biomes);
    }

    public int getMetadataMax() {
        int i = -1;
        i = getMax(metadatas, i);

        if (matchBlocks != null) {
            for (MatchBlock matchblock : matchBlocks) {
                i = getMax(matchblock.getMetadatas(), i);
            }
        }

        return i;
    }

    private int getMax(int[] mds, int max) {
        if (mds != null) {
            for (int j : mds) {
                if (j > max) {
                    max = j;
                }
            }

        }
        return max;
    }
}
