package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class NBTTagIntArray extends NBTBase {
    private int[] intArray;

    NBTTagIntArray() {
    }

    public NBTTagIntArray(int[] p_i45132_1_) {
        intArray = p_i45132_1_;
    }

    void write(DataOutput output) throws IOException {
        output.writeInt(intArray.length);

        for (int j : intArray) {
            output.writeInt(j);
        }
    }

    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(192L);
        int i = input.readInt();
        sizeTracker.read(32L * i);
        intArray = new int[i];

        for (int j = 0; j < i; ++j) {
            intArray[j] = input.readInt();
        }
    }

    public byte getId() {
        return (byte) 11;
    }

    public String toString() {
        StringBuilder s = new StringBuilder("[");

        for (int i : intArray) {
            s.append(i).append(",");
        }

        return s + "]";
    }

    public NBTBase copy() {
        int[] aint = new int[intArray.length];
        System.arraycopy(intArray, 0, aint, 0, intArray.length);
        return new NBTTagIntArray(aint);
    }

    public boolean equals(Object p_equals_1_) {
        return super.equals(p_equals_1_) && Arrays.equals(intArray, ((NBTTagIntArray) p_equals_1_).intArray);
    }

    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(intArray);
    }

    public int[] getIntArray() {
        return intArray;
    }
}
