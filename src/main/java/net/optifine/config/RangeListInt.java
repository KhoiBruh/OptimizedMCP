package net.optifine.config;

import net.minecraft.src.Config;

public class RangeListInt {
    private RangeInt[] ranges = new RangeInt[0];

    public RangeListInt() {
    }

    public RangeListInt(RangeInt ri) {
        addRange(ri);
    }

    public void addRange(RangeInt ri) {
        ranges = (RangeInt[]) Config.addObjectToArray(ranges, ri);
    }

    public boolean isInRange(int val) {
        for (RangeInt rangeint : ranges) {
            if (rangeint.isInRange(val)) {
                return true;
            }
        }

        return false;
    }

    public int getCountRanges() {
        return ranges.length;
    }

    public RangeInt getRange(int i) {
        return ranges[i];
    }

    public String toString() {
        StringBuilder stringbuffer = new StringBuilder();
        stringbuffer.append("[");

        for (int i = 0; i < ranges.length; ++i) {
            RangeInt rangeint = ranges[i];

            if (i > 0) {
                stringbuffer.append(", ");
            }

            stringbuffer.append(rangeint.toString());
        }

        stringbuffer.append("]");
        return stringbuffer.toString();
    }
}
