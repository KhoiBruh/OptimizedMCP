package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class NBTTagByteArray extends NBTBase {
    private byte[] data;

    NBTTagByteArray() {
    }

    public NBTTagByteArray(byte[] data) {
        this.data = data;
    }

    void write(DataOutput output) throws IOException {
        output.writeInt(data.length);
        output.write(data);
    }

    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(192L);
        int i = input.readInt();
        sizeTracker.read(8L * i);
        data = new byte[i];
        input.readFully(data);
    }

    public byte getId() {
        return (byte) 7;
    }

    public String toString() {
        return "[" + data.length + " bytes]";
    }

    public NBTBase copy() {
        byte[] abyte = new byte[data.length];
        System.arraycopy(data, 0, abyte, 0, data.length);
        return new NBTTagByteArray(abyte);
    }

    public boolean equals(Object p_equals_1_) {
        return super.equals(p_equals_1_) && Arrays.equals(data, ((NBTTagByteArray) p_equals_1_).data);
    }

    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(data);
    }

    public byte[] getByteArray() {
        return data;
    }
}
