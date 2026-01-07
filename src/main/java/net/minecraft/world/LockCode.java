package net.minecraft.world;

import net.minecraft.nbt.NBTTagCompound;

public record LockCode(String lock) {
    public static final LockCode EMPTY_CODE = new LockCode("");

    public static LockCode fromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("Lock", 8)) {
            String s = nbt.getString("Lock");
            return new LockCode(s);
        } else {
            return EMPTY_CODE;
        }
    }

    public boolean isEmpty() {
        return lock == null || lock.isEmpty();
    }

    public void toNBT(NBTTagCompound nbt) {
        nbt.setString("Lock", lock);
    }
}
