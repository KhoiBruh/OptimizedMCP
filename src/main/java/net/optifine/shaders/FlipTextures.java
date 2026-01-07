package net.optifine.shaders;

import java.nio.IntBuffer;
import java.util.Arrays;

public class FlipTextures {
    private final IntBuffer textures;
    private final int indexFlipped;
    private final boolean[] flips;
    private final boolean[] changed;

    public FlipTextures(IntBuffer textures, int indexFlipped) {
        this.textures = textures;
        this.indexFlipped = indexFlipped;
        flips = new boolean[textures.capacity()];
        changed = new boolean[textures.capacity()];
    }

    public int getA(int index) {
        return get(index, flips[index]);
    }

    public int getB(int index) {
        return get(index, !flips[index]);
    }

    private int get(int index, boolean flipped) {
        int i = flipped ? indexFlipped : 0;
        return textures.get(i + index);
    }

    public void flip(int index) {
        flips[index] = !flips[index];
        changed[index] = true;
    }

    public boolean isChanged(int index) {
        return changed[index];
    }

    public void reset() {
        Arrays.fill(flips, false);
        Arrays.fill(changed, false);
    }
}
