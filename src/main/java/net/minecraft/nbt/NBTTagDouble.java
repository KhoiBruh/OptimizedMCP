package net.minecraft.nbt;

import net.minecraft.util.MathHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagDouble extends NBTBase.NBTPrimitive {
    private double data;

    NBTTagDouble() {
    }

    public NBTTagDouble(double data) {
        this.data = data;
    }

    void write(DataOutput output) throws IOException {
        output.writeDouble(data);
    }

    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(128L);
        data = input.readDouble();
    }

    public byte getId() {
        return (byte) 6;
    }

    public String toString() {
        return data + "d";
    }

    public NBTBase copy() {
        return new NBTTagDouble(data);
    }

    public boolean equals(Object p_equals_1_) {
        if (super.equals(p_equals_1_)) {
            NBTTagDouble nbttagdouble = (NBTTagDouble) p_equals_1_;
            return data == nbttagdouble.data;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return super.hashCode() ^ Double.hashCode(data);
    }

    public long getLong() {
        return (long) Math.floor(data);
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
        return (float) data;
    }
}
