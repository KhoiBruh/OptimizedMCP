package net.minecraft.village;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VillageCollection extends WorldSavedData {
    private final List<BlockPos> villagerPositionsList = new ArrayList<>();
    private final List<VillageDoorInfo> newDoors = new ArrayList<>();
    private final List<Village> villageList = new ArrayList<>();
    private World worldObj;
    private int tickCounter;

    public VillageCollection(String name) {
        super(name);
    }

    public VillageCollection(World worldIn) {
        super(fileNameForProvider(worldIn.provider));
        worldObj = worldIn;
        markDirty();
    }

    public static String fileNameForProvider(WorldProvider provider) {
        return "villages" + provider.getInternalNameSuffix();
    }

    public void setWorldsForAll(World worldIn) {
        worldObj = worldIn;

        for (Village village : villageList) {
            village.setWorld(worldIn);
        }
    }

    public void addToVillagerPositionList(BlockPos pos) {
        if (villagerPositionsList.size() <= 64) {
            if (!positionInList(pos)) {
                villagerPositionsList.add(pos);
            }
        }
    }

    public void tick() {
        ++tickCounter;

        for (Village village : villageList) {
            village.tick(tickCounter);
        }

        removeAnnihilatedVillages();
        dropOldestVillagerPosition();
        addNewDoorsToVillageOrCreateVillage();

        if (tickCounter % 400 == 0) {
            markDirty();
        }
    }

    private void removeAnnihilatedVillages() {
        Iterator<Village> iterator = villageList.iterator();

        while (iterator.hasNext()) {
            Village village = iterator.next();

            if (village.isAnnihilated()) {
                iterator.remove();
                markDirty();
            }
        }
    }

    public List<Village> getVillageList() {
        return villageList;
    }

    public Village getNearestVillage(BlockPos doorBlock, int radius) {
        Village village = null;
        double d0 = 3.4028234663852886E38D;

        for (Village village1 : villageList) {
            double d1 = village1.getCenter().distanceSq(doorBlock);

            if (d1 < d0) {
                float f = (float) (radius + village1.getVillageRadius());

                if (d1 <= (double) (f * f)) {
                    village = village1;
                    d0 = d1;
                }
            }
        }

        return village;
    }

    private void dropOldestVillagerPosition() {
        if (!villagerPositionsList.isEmpty()) {
            addDoorsAround(villagerPositionsList.removeFirst());
        }
    }

    private void addNewDoorsToVillageOrCreateVillage() {
        for (VillageDoorInfo villagedoorinfo : newDoors) {
            Village village = getNearestVillage(villagedoorinfo.getDoorBlockPos(), 32);

            if (village == null) {
                village = new Village(worldObj);
                villageList.add(village);
                markDirty();
            }

            village.addVillageDoorInfo(villagedoorinfo);
        }

        newDoors.clear();
    }

    private void addDoorsAround(BlockPos central) {
        int i = 16;
        int j = 4;
        int k = 16;

        for (int l = -i; l < i; ++l) {
            for (int i1 = -j; i1 < j; ++i1) {
                for (int j1 = -k; j1 < k; ++j1) {
                    BlockPos blockpos = central.add(l, i1, j1);

                    if (isWoodDoor(blockpos)) {
                        VillageDoorInfo villagedoorinfo = checkDoorExistence(blockpos);

                        if (villagedoorinfo == null) {
                            addToNewDoorsList(blockpos);
                        } else {
                            villagedoorinfo.func_179849_a(tickCounter);
                        }
                    }
                }
            }
        }
    }

    private VillageDoorInfo checkDoorExistence(BlockPos doorBlock) {
        for (VillageDoorInfo villagedoorinfo : newDoors) {
            if (villagedoorinfo.getDoorBlockPos().getX() == doorBlock.getX() && villagedoorinfo.getDoorBlockPos().getZ() == doorBlock.getZ() && Math.abs(villagedoorinfo.getDoorBlockPos().getY() - doorBlock.getY()) <= 1) {
                return villagedoorinfo;
            }
        }

        for (Village village : villageList) {
            VillageDoorInfo villagedoorinfo1 = village.getExistedDoor(doorBlock);

            if (villagedoorinfo1 != null) {
                return villagedoorinfo1;
            }
        }

        return null;
    }

    private void addToNewDoorsList(BlockPos doorBlock) {
        EnumFacing enumfacing = BlockDoor.getFacing(worldObj, doorBlock);
        EnumFacing enumfacing1 = enumfacing.getOpposite();
        int i = countBlocksCanSeeSky(doorBlock, enumfacing, 5);
        int j = countBlocksCanSeeSky(doorBlock, enumfacing1, i + 1);

        if (i != j) {
            newDoors.add(new VillageDoorInfo(doorBlock, i < j ? enumfacing : enumfacing1, tickCounter));
        }
    }

    private int countBlocksCanSeeSky(BlockPos centerPos, EnumFacing direction, int limitation) {
        int i = 0;

        for (int j = 1; j <= 5; ++j) {
            if (worldObj.canSeeSky(centerPos.offset(direction, j))) {
                ++i;

                if (i >= limitation) {
                    return i;
                }
            }
        }

        return i;
    }

    private boolean positionInList(BlockPos pos) {
        for (BlockPos blockpos : villagerPositionsList) {
            if (blockpos.equals(pos)) {
                return true;
            }
        }

        return false;
    }

    private boolean isWoodDoor(BlockPos doorPos) {
        Block block = worldObj.getBlockState(doorPos).getBlock();
        return block instanceof BlockDoor && block.getMaterial() == Material.wood;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        tickCounter = nbt.getInteger("Tick");
        NBTTagList nbttaglist = nbt.getTagList("Villages", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            Village village = new Village();
            village.readVillageDataFromNBT(nbttagcompound);
            villageList.add(village);
        }
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("Tick", tickCounter);
        NBTTagList nbttaglist = new NBTTagList();

        for (Village village : villageList) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            village.writeVillageDataToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
        }

        nbt.setTag("Villages", nbttaglist);
    }
}
