package net.minecraft.client.renderer.chunk;

import net.minecraft.util.EnumFacing;

import java.util.Set;

public class SetVisibility {
    private static final int COUNT_FACES = EnumFacing.values().length;
    private long bits;

    public void setManyVisible(Set<EnumFacing> p_178620_1_) {
        for (EnumFacing enumfacing : p_178620_1_) {
            for (EnumFacing enumfacing1 : p_178620_1_) {
                setVisible(enumfacing, enumfacing1, true);
            }
        }
    }

    public void setVisible(EnumFacing facing, EnumFacing facing2, boolean p_178619_3_) {
        setBit(facing.ordinal() + facing2.ordinal() * COUNT_FACES, p_178619_3_);
        setBit(facing2.ordinal() + facing.ordinal() * COUNT_FACES, p_178619_3_);
    }

    public void setAllVisible(boolean visible) {
        if (visible) {
            bits = -1L;
        } else {
            bits = 0L;
        }
    }

    public boolean isVisible(EnumFacing facing, EnumFacing facing2) {
        return getBit(facing.ordinal() + facing2.ordinal() * COUNT_FACES);
    }

    public String toString() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(' ');

        for (EnumFacing enumfacing : EnumFacing.values()) {
            stringbuilder.append(' ').append(enumfacing.toString().toUpperCase().charAt(0));
        }

        stringbuilder.append('\n');

        for (EnumFacing enumfacing2 : EnumFacing.values()) {
            stringbuilder.append(enumfacing2.toString().toUpperCase().charAt(0));

            for (EnumFacing enumfacing1 : EnumFacing.values()) {
                if (enumfacing2 == enumfacing1) {
                    stringbuilder.append("  ");
                } else {
                    boolean flag = isVisible(enumfacing2, enumfacing1);
                    stringbuilder.append(' ').append(flag ? 'Y' : 'n');
                }
            }

            stringbuilder.append('\n');
        }

        return stringbuilder.toString();
    }

    private boolean getBit(int p_getBit_1_) {
        return (bits & (1L << p_getBit_1_)) != 0L;
    }

    private void setBit(int p_setBit_1_, boolean p_setBit_2_) {
        if (p_setBit_2_) {
            setBit(p_setBit_1_);
        } else {
            clearBit(p_setBit_1_);
        }
    }

    private void setBit(int p_setBit_1_) {
        bits |= 1L << p_setBit_1_;
    }

    private void clearBit(int p_clearBit_1_) {
        bits &= ~(1L << p_clearBit_1_);
    }
}
