package net.minecraft.nbt;

import net.minecraft.util.MathHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagFloat extends NBTBase.NBTPrimitive {
    private float data;

    NBTTagFloat() {
    }

    public NBTTagFloat(float data) {
        this.data = data;
    }

    void write(DataOutput output) throws IOException {
        output.writeFloat(data);
    }

    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(96L);
        data = input.readFloat();
    }

    public byte getId() {
        return (byte) 5;
    }

    public String toString() {
        return data + "f";
    }

    public NBTBase copy() {
        return new NBTTagFloat(data);
    }

    public boolean equals(Object p_equals_1_) {
        if (super.equals(p_equals_1_)) {
            NBTTagFloat nbttagfloat = (NBTTagFloat) p_equals_1_;
            return data == nbttagfloat.data;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return super.hashCode() ^ Float.floatToIntBits(data);
    }

    public long getLong() {
        return (long) data;
    }

    public int getInt() {
        return MathHelper.floor(data);
    }

    public short getShort() {
        return (short) (MathHelper.floor(data) & 65535);
    }

    public byte getByte() {
        return (byte) (MathHelper.floor(data) & 255);
    }

    public double getDouble() {
        return data;
    }

    public float getFloat() {
        return data;
    }
}
