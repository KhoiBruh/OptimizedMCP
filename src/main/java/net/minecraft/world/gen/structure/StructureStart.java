package net.minecraft.world.gen.structure;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.Random;

public abstract class StructureStart {
    protected LinkedList<StructureComponent> components = new LinkedList<>();
    protected StructureBoundingBox boundingBox;
    private int chunkPosX;
    private int chunkPosZ;

    public StructureStart() {
    }

    public StructureStart(int chunkX, int chunkZ) {
        chunkPosX = chunkX;
        chunkPosZ = chunkZ;
    }

    public StructureBoundingBox getBoundingBox() {
        return boundingBox;
    }

    public LinkedList<StructureComponent> getComponents() {
        return components;
    }

    public void generateStructure(World worldIn, Random rand, StructureBoundingBox structurebb) {

        components.removeIf(structurecomponent -> structurecomponent.getBoundingBox().intersectsWith(structurebb) && !structurecomponent.addComponentParts(worldIn, rand, structurebb));
    }

    protected void updateBoundingBox() {
        boundingBox = StructureBoundingBox.getNewBoundingBox();

        for (StructureComponent structurecomponent : components) {
            boundingBox.expandTo(structurecomponent.getBoundingBox());
        }
    }

    public NBTTagCompound writeStructureComponentsToNBT(int chunkX, int chunkZ) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("id", MapGenStructureIO.getStructureStartName(this));
        nbttagcompound.setInteger("ChunkX", chunkX);
        nbttagcompound.setInteger("ChunkZ", chunkZ);
        nbttagcompound.setTag("BB", boundingBox.toNBTTagIntArray());
        NBTTagList nbttaglist = new NBTTagList();

        for (StructureComponent structurecomponent : components) {
            nbttaglist.appendTag(structurecomponent.createStructureBaseNBT());
        }

        nbttagcompound.setTag("Children", nbttaglist);
        writeToNBT(nbttagcompound);
        return nbttagcompound;
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
    }

    public void readStructureComponentsFromNBT(World worldIn, NBTTagCompound tagCompound) {
        chunkPosX = tagCompound.getInteger("ChunkX");
        chunkPosZ = tagCompound.getInteger("ChunkZ");

        if (tagCompound.hasKey("BB")) {
            boundingBox = new StructureBoundingBox(tagCompound.getIntArray("BB"));
        }

        NBTTagList nbttaglist = tagCompound.getTagList("Children", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            components.add(MapGenStructureIO.getStructureComponent(nbttaglist.getCompoundTagAt(i), worldIn));
        }

        readFromNBT(tagCompound);
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
    }

    protected void markAvailableHeight(World worldIn, Random rand, int p_75067_3_) {
        int i = worldIn.getSeaLevel() - p_75067_3_;
        int j = boundingBox.getYSize() + 1;

        if (j < i) {
            j += rand.nextInt(i - j);
        }

        int k = j - boundingBox.maxY;
        boundingBox.offset(0, k, 0);

        for (StructureComponent structurecomponent : components) {
            structurecomponent.func_181138_a(0, k, 0);
        }
    }

    protected void setRandomHeight(World worldIn, Random rand, int p_75070_3_, int p_75070_4_) {
        int i = p_75070_4_ - p_75070_3_ + 1 - boundingBox.getYSize();
        int j = 1;

        if (i > 1) {
            j = p_75070_3_ + rand.nextInt(i);
        } else {
            j = p_75070_3_;
        }

        int k = j - boundingBox.minY;
        boundingBox.offset(0, k, 0);

        for (StructureComponent structurecomponent : components) {
            structurecomponent.func_181138_a(0, k, 0);
        }
    }

    public boolean isSizeableStructure() {
        return true;
    }

    public boolean func_175788_a(ChunkCoordIntPair pair) {
        return true;
    }

    public void func_175787_b(ChunkCoordIntPair pair) {
    }

    public int getChunkPosX() {
        return chunkPosX;
    }

    public int getChunkPosZ() {
        return chunkPosZ;
    }
}
