package net.minecraft.nbt;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

public class NBTTagList extends NBTBase {
    private static final Logger LOGGER = LogManager.getLogger();
    private List<NBTBase> tagList = Lists.newArrayList();
    private byte tagType = 0;

    void write(DataOutput output) throws IOException {
        if (!tagList.isEmpty()) {
            tagType = tagList.getFirst().getId();
        } else {
            tagType = 0;
        }

        output.writeByte(tagType);
        output.writeInt(tagList.size());

        for (NBTBase nbtBase : tagList) {
            nbtBase.write(output);
        }
    }

    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(296L);

        if (depth > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        } else {
            tagType = input.readByte();
            int i = input.readInt();

            if (tagType == 0 && i > 0) {
                throw new RuntimeException("Missing type on ListTag");
            } else {
                sizeTracker.read(32L * (long) i);
                tagList = Lists.newArrayListWithCapacity(i);

                for (int j = 0; j < i; ++j) {
                    NBTBase nbtbase = NBTBase.createNewByType(tagType);
                    nbtbase.read(input, depth + 1, sizeTracker);
                    tagList.add(nbtbase);
                }
            }
        }
    }

    public byte getId() {
        return (byte) 9;
    }

    public String toString() {
        StringBuilder stringbuilder = new StringBuilder("[");

        for (int i = 0; i < tagList.size(); ++i) {
            if (i != 0) {
                stringbuilder.append(',');
            }

            stringbuilder.append(i).append(':').append(tagList.get(i));
        }

        return stringbuilder.append(']').toString();
    }

    public void appendTag(NBTBase nbt) {
        if (nbt.getId() == 0) {
            LOGGER.warn("Invalid TagEnd added to ListTag");
        } else {
            if (tagType == 0) {
                tagType = nbt.getId();
            } else if (tagType != nbt.getId()) {
                LOGGER.warn("Adding mismatching tag types to tag list");
                return;
            }

            tagList.add(nbt);
        }
    }

    public void set(int idx, NBTBase nbt) {
        if (nbt.getId() == 0) {
            LOGGER.warn("Invalid TagEnd added to ListTag");
        } else if (idx >= 0 && idx < tagList.size()) {
            if (tagType == 0) {
                tagType = nbt.getId();
            } else if (tagType != nbt.getId()) {
                LOGGER.warn("Adding mismatching tag types to tag list");
                return;
            }

            tagList.set(idx, nbt);
        } else {
            LOGGER.warn("index out of bounds to set tag in tag list");
        }
    }

    public NBTBase removeTag(int i) {
        return tagList.remove(i);
    }

    public boolean hasNoTags() {
        return tagList.isEmpty();
    }

    public NBTTagCompound getCompoundTagAt(int i) {
        if (i >= 0 && i < tagList.size()) {
            NBTBase nbtbase = tagList.get(i);
            return nbtbase.getId() == 10 ? (NBTTagCompound) nbtbase : new NBTTagCompound();
        } else {
            return new NBTTagCompound();
        }
    }

    public int[] getIntArrayAt(int i) {
        if (i >= 0 && i < tagList.size()) {
            NBTBase nbtbase = tagList.get(i);
            return nbtbase.getId() == 11 ? ((NBTTagIntArray) nbtbase).getIntArray() : new int[0];
        } else {
            return new int[0];
        }
    }

    public double getDoubleAt(int i) {
        if (i >= 0 && i < tagList.size()) {
            NBTBase nbtbase = tagList.get(i);
            return nbtbase.getId() == 6 ? ((NBTTagDouble) nbtbase).getDouble() : 0.0D;
        } else {
            return 0.0D;
        }
    }

    public float getFloatAt(int i) {
        if (i >= 0 && i < tagList.size()) {
            NBTBase nbtbase = tagList.get(i);
            return nbtbase.getId() == 5 ? ((NBTTagFloat) nbtbase).getFloat() : 0.0F;
        } else {
            return 0.0F;
        }
    }

    public String getStringTagAt(int i) {
        if (i >= 0 && i < tagList.size()) {
            NBTBase nbtbase = tagList.get(i);
            return nbtbase.getId() == 8 ? nbtbase.getString() : nbtbase.toString();
        } else {
            return "";
        }
    }

    public NBTBase get(int idx) {
        return idx >= 0 && idx < tagList.size() ? tagList.get(idx) : new NBTTagEnd();
    }

    public int tagCount() {
        return tagList.size();
    }

    public NBTBase copy() {
        NBTTagList nbttaglist = new NBTTagList();
        nbttaglist.tagType = tagType;

        for (NBTBase nbtbase : tagList) {
            NBTBase nbtbase1 = nbtbase.copy();
            nbttaglist.tagList.add(nbtbase1);
        }

        return nbttaglist;
    }

    public boolean equals(Object p_equals_1_) {
        if (super.equals(p_equals_1_)) {
            NBTTagList nbttaglist = (NBTTagList) p_equals_1_;

            if (tagType == nbttaglist.tagType) {
                return tagList.equals(nbttaglist.tagList);
            }
        }

        return false;
    }

    public int hashCode() {
        return super.hashCode() ^ tagList.hashCode();
    }

    public int getTagType() {
        return tagType;
    }
}
