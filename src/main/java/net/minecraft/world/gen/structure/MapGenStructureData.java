package net.minecraft.world.gen.structure;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

public class MapGenStructureData extends WorldSavedData {
    private NBTTagCompound tagCompound = new NBTTagCompound();

    public MapGenStructureData(String name) {
        super(name);
    }

    public static String formatChunkCoords(int chunkX, int chunkZ) {
        return "[" + chunkX + "," + chunkZ + "]";
    }

    public void readFromNBT(NBTTagCompound nbt) {
        tagCompound = nbt.getCompoundTag("Features");
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("Features", tagCompound);
    }

    public void writeInstance(NBTTagCompound tagCompoundIn, int chunkX, int chunkZ) {
        tagCompound.setTag(formatChunkCoords(chunkX, chunkZ), tagCompoundIn);
    }

    public NBTTagCompound getTagCompound() {
        return tagCompound;
    }
}
