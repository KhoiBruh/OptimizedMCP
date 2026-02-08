package net.optifine;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.src.Config;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;

import java.util.IdentityHashMap;
import java.util.Map;

public class NaturalProperties {
    public int rotation = 1;
    public boolean flip = false;
    private final Map<BakedQuad, BakedQuad>[] quadMaps = new Map[8];

    public NaturalProperties(String type) {
        switch (type) {
            case "4" -> rotation = 4;
            case "2" -> rotation = 2;
            case "F" -> flip = true;
            case "4F" -> {
                rotation = 4;
                flip = true;
            }
            case "2F" -> {
                rotation = 2;
                flip = true;
            }
            default -> Config.warn("NaturalTextures: Unknown type: " + type);
        }
    }

    public boolean isValid() {
        return rotation == 2 || rotation == 4 || flip;
    }

    public synchronized BakedQuad getQuad(BakedQuad quadIn, int rotate, boolean flipU) {
        int i = rotate;

        if (flipU) {
            i = rotate | 4;
        }

        if (i > 0 && i < quadMaps.length) {
            Map<BakedQuad, BakedQuad> map = quadMaps[i];

            if (map == null) {
                map = new IdentityHashMap<>(1);
                quadMaps[i] = map;
            }

            BakedQuad bakedquad = map.get(quadIn);

            if (bakedquad == null) {
                bakedquad = makeQuad(quadIn, rotate, flipU);
                map.put(quadIn, bakedquad);
            }

            return bakedquad;
        } else {
            return quadIn;
        }
    }

    private BakedQuad makeQuad(BakedQuad quad, int rotate, boolean flipU) {
        int[] aint = quad.getVertexData();
        int i = quad.getTintIndex();
        Direction enumfacing = quad.getFace();
        TextureAtlasSprite textureatlassprite = quad.getSprite();

        if (!isFullSprite(quad)) {
            rotate = 0;
        }

        aint = transformVertexData(aint, rotate, flipU);
        return new BakedQuad(aint, i, enumfacing, textureatlassprite);
    }

    private int[] transformVertexData(int[] vertexData, int rotate, boolean flipU) {
        int[] aint = vertexData.clone();
        int i = 4 - rotate;

        if (flipU) {
            i += 3;
        }

        i = i % 4;
        int j = aint.length / 4;

        for (int k = 0; k < 4; ++k) {
            int l = k * j;
            int i1 = i * j;
            aint[i1 + 4] = vertexData[l + 4];
            aint[i1 + 4 + 1] = vertexData[l + 4 + 1];

            if (flipU) {
                --i;

                if (i < 0) {
                    i = 3;
                }
            } else {
                ++i;

                if (i > 3) {
                    i = 0;
                }
            }
        }

        return aint;
    }

    private boolean isFullSprite(BakedQuad quad) {
        TextureAtlasSprite textureatlassprite = quad.getSprite();
        float f = textureatlassprite.getMinU();
        float f1 = textureatlassprite.getMaxU();
        float f2 = f1 - f;
        float f3 = f2 / 256.0F;
        float f4 = textureatlassprite.getMinV();
        float f5 = textureatlassprite.getMaxV();
        float f6 = f5 - f4;
        float f7 = f6 / 256.0F;
        int[] aint = quad.getVertexData();
        int i = aint.length / 4;

        for (int j = 0; j < 4; ++j) {
            int k = j * i;
            float f8 = Float.intBitsToFloat(aint[k + 4]);
            float f9 = Float.intBitsToFloat(aint[k + 4 + 1]);

            if (!equalsDelta(f8, f, f3) && !equalsDelta(f8, f1, f3)) {
                return false;
            }

            if (!equalsDelta(f9, f4, f7) && !equalsDelta(f9, f5, f7)) {
                return false;
            }
        }

        return true;
    }

    private boolean equalsDelta(float x1, float x2, float deltaMax) {
        float f = MathHelper.abs(x1 - x2);
        return f < deltaMax;
    }
}
