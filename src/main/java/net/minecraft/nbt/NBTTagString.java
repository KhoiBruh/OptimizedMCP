package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagString extends NBTBase {
    private String data;

    public NBTTagString() {
        data = "";
    }

    public NBTTagString(String data) {
        this.data = data;

        if (data == null) {
            throw new IllegalArgumentException("Empty string not allowed");
        }
    }

    void write(DataOutput output) throws IOException {
        output.writeUTF(data);
    }

    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(288L);
        data = input.readUTF();
        sizeTracker.read(16L * data.length());
    }

    public byte getId() {
        return (byte) 8;
    }

    public String toString() {
        return "\"" + data.replace("\"", "\\\"") + "\"";
    }

    public NBTBase copy() {
        return new NBTTagString(data);
    }

    public boolean hasNoTags() {
        return data.isEmpty();
    }

    public boolean equals(Object p_equals_1_) {
        if (!super.equals(p_equals_1_)) {
            return false;
        } else {
            NBTTagString nbttagstring = (NBTTagString) p_equals_1_;
            return data == null && nbttagstring.data == null || data != null && data.equals(nbttagstring.data);
        }
    }

    public int hashCode() {
        return super.hashCode() ^ data.hashCode();
    }

    public String getString() {
        return data;
    }
}
