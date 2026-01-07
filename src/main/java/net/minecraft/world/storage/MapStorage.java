package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.world.WorldSavedData;

import java.io.*;
import java.util.List;
import java.util.Map;

public class MapStorage {
    protected Map<String, WorldSavedData> loadedDataMap = Maps.newHashMap();
    private final ISaveHandler saveHandler;
    private final List<WorldSavedData> loadedDataList = Lists.newArrayList();
    private final Map<String, Short> idCounts = Maps.newHashMap();

    public MapStorage(ISaveHandler saveHandlerIn) {
        saveHandler = saveHandlerIn;
        loadIdCounts();
    }

    public WorldSavedData loadData(Class<? extends WorldSavedData> clazz, String dataIdentifier) {
        WorldSavedData worldsaveddata = loadedDataMap.get(dataIdentifier);

        if (worldsaveddata != null) {
            return worldsaveddata;
        } else {
            if (saveHandler != null) {
                try {
                    File file1 = saveHandler.getMapFileFromName(dataIdentifier);

                    if (file1 != null && file1.exists()) {
                        try {
                            worldsaveddata = clazz.getConstructor(new Class[]{String.class}).newInstance(new Object[]{dataIdentifier});
                        } catch (Exception exception) {
                            throw new RuntimeException("Failed to instantiate " + clazz.toString(), exception);
                        }

                        FileInputStream fileinputstream = new FileInputStream(file1);
                        NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                        fileinputstream.close();
                        worldsaveddata.readFromNBT(nbttagcompound.getCompoundTag("data"));
                    }
                } catch (Exception exception1) {
                    exception1.printStackTrace();
                }
            }

            if (worldsaveddata != null) {
                loadedDataMap.put(dataIdentifier, worldsaveddata);
                loadedDataList.add(worldsaveddata);
            }

            return worldsaveddata;
        }
    }

    public void setData(String dataIdentifier, WorldSavedData data) {
        if (loadedDataMap.containsKey(dataIdentifier)) {
            loadedDataList.remove(loadedDataMap.remove(dataIdentifier));
        }

        loadedDataMap.put(dataIdentifier, data);
        loadedDataList.add(data);
    }

    public void saveAllData() {
        for (WorldSavedData worldsaveddata : loadedDataList) {
            if (worldsaveddata.isDirty()) {
                saveData(worldsaveddata);
                worldsaveddata.setDirty(false);
            }
        }
    }

    private void saveData(WorldSavedData p_75747_1_) {
        if (saveHandler != null) {
            try {
                File file1 = saveHandler.getMapFileFromName(p_75747_1_.mapName);

                if (file1 != null) {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    p_75747_1_.writeToNBT(nbttagcompound);
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setTag("data", nbttagcompound);
                    FileOutputStream fileoutputstream = new FileOutputStream(file1);
                    CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
                    fileoutputstream.close();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private void loadIdCounts() {
        try {
            idCounts.clear();

            if (saveHandler == null) {
                return;
            }

            File file1 = saveHandler.getMapFileFromName("idcounts");

            if (file1 != null && file1.exists()) {
                DataInputStream datainputstream = new DataInputStream(new FileInputStream(file1));
                NBTTagCompound nbttagcompound = CompressedStreamTools.read(datainputstream);
                datainputstream.close();

                for (String s : nbttagcompound.getKeySet()) {
                    NBTBase nbtbase = nbttagcompound.getTag(s);

                    if (nbtbase instanceof NBTTagShort nbttagshort) {
                        short short1 = nbttagshort.getShort();
                        idCounts.put(s, short1);
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public int getUniqueDataId(String key) {
        Short oshort = idCounts.get(key);

        if (oshort == null) {
            oshort = (short) 0;
        } else {
            oshort = (short) (oshort.shortValue() + 1);
        }

        idCounts.put(key, oshort);

        if (saveHandler == null) {
            return oshort;
        } else {
            try {
                File file1 = saveHandler.getMapFileFromName("idcounts");

                if (file1 != null) {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();

                    for (String s : idCounts.keySet()) {
                        short short1 = idCounts.get(s);
                        nbttagcompound.setShort(s, short1);
                    }

                    DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file1));
                    CompressedStreamTools.write(nbttagcompound, dataoutputstream);
                    dataoutputstream.close();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            return oshort;
        }
    }
}
