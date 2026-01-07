package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagShort extends NBTBase.NBTPrimitive {
    private short data;

    public NBTTagShort() {
    }

    public NBTTagShort(short data) {
        this.data = data;
    }

    void write(DataOutput output) throws IOException {
        output.writeShort(data);
    }

    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(80L);
        data = input.readShort();
    }

    public byte getId() {
        return (byte) 2;
    }

    public String toString() {
        return data + "s";
    }

    public NBTBase copy() {
        return new NBTTagShort(data);
    }

    public boolean equals(Object p_equals_1_) {
        if (super.equals(p_equals_1_)) {
            NBTTagShort nbttagshort = (NBTTagShort) p_equals_1_;
            return data == nbttagshort.data;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return super.hashCode() ^ data;
    }

    public long getLong() {
        return data;
    }

    public int getInt() {
        return data;
    }

    public short getShort() {
        return data;
    }

    public byte getByte() {
        return (byte) (data & 255);
    }

    public double getDouble() {
        return data;
    }

    public float getFloat() {
        return data;
    }
}
