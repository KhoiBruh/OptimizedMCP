package net.minecraft.world;

import net.minecraft.nbt.NBTTagCompound;

public abstract class WorldSavedData {
    public final String mapName;
    private boolean dirty;

    public WorldSavedData(String name) {
        mapName = name;
    }

    public abstract void readFromNBT(NBTTagCompound nbt);

    public abstract void writeToNBT(NBTTagCompound nbt);

    public void markDirty() {
        setDirty(true);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean isDirty) {
        dirty = isDirty;
    }
}
