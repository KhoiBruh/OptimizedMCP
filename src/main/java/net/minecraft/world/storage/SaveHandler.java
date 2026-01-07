package net.minecraft.world.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class SaveHandler implements ISaveHandler, IPlayerFileData {
    private static final Logger logger = LogManager.getLogger();
    private final File worldDirectory;
    private final File playersDirectory;
    private final File mapDataDir;
    private final long initializationTime = MinecraftServer.getCurrentTimeMillis();
    private final String saveDirectoryName;

    public SaveHandler(File savesDirectory, String directoryName, boolean playersDirectoryIn) {
        worldDirectory = new File(savesDirectory, directoryName);
        worldDirectory.mkdirs();
        playersDirectory = new File(worldDirectory, "playerdata");
        mapDataDir = new File(worldDirectory, "data");
        mapDataDir.mkdirs();
        saveDirectoryName = directoryName;

        if (playersDirectoryIn) {
            playersDirectory.mkdirs();
        }

        setSessionLock();
    }

    private void setSessionLock() {
        try {
            File file1 = new File(worldDirectory, "session.lock");
            DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file1));

            try {
                dataoutputstream.writeLong(initializationTime);
            } finally {
                dataoutputstream.close();
            }
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
            throw new RuntimeException("Failed to check session lock, aborting");
        }
    }

    public File getWorldDirectory() {
        return worldDirectory;
    }

    public void checkSessionLock() throws MinecraftException {
        try {
            File file1 = new File(worldDirectory, "session.lock");
            DataInputStream datainputstream = new DataInputStream(new FileInputStream(file1));

            try {
                if (datainputstream.readLong() != initializationTime) {
                    throw new MinecraftException("The save is being accessed from another location, aborting");
                }
            } finally {
                datainputstream.close();
            }
        } catch (IOException var7) {
            throw new MinecraftException("Failed to check session lock, aborting");
        }
    }

    public IChunkLoader getChunkLoader(WorldProvider provider) {
        throw new RuntimeException("Old Chunk Storage is no longer supported.");
    }

    public WorldInfo loadWorldInfo() {
        File file1 = new File(worldDirectory, "level.dat");

        if (file1.exists()) {
            try {
                NBTTagCompound nbttagcompound2 = CompressedStreamTools.readCompressed(new FileInputStream(file1));
                NBTTagCompound nbttagcompound3 = nbttagcompound2.getCompoundTag("Data");
                return new WorldInfo(nbttagcompound3);
            } catch (Exception exception1) {
                exception1.printStackTrace();
            }
        }

        file1 = new File(worldDirectory, "level.dat_old");

        if (file1.exists()) {
            try {
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file1));
                NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Data");
                return new WorldInfo(nbttagcompound1);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        return null;
    }

    public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {
        NBTTagCompound nbttagcompound = worldInformation.cloneNBTCompound(tagCompound);
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        nbttagcompound1.setTag("Data", nbttagcompound);

        try {
            File file1 = new File(worldDirectory, "level.dat_new");
            File file2 = new File(worldDirectory, "level.dat_old");
            File file3 = new File(worldDirectory, "level.dat");
            CompressedStreamTools.writeCompressed(nbttagcompound1, new FileOutputStream(file1));

            if (file2.exists()) {
                file2.delete();
            }

            file3.renameTo(file2);

            if (file3.exists()) {
                file3.delete();
            }

            file1.renameTo(file3);

            if (file1.exists()) {
                file1.delete();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void saveWorldInfo(WorldInfo worldInformation) {
        NBTTagCompound nbttagcompound = worldInformation.getNBTTagCompound();
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        nbttagcompound1.setTag("Data", nbttagcompound);

        try {
            File file1 = new File(worldDirectory, "level.dat_new");
            File file2 = new File(worldDirectory, "level.dat_old");
            File file3 = new File(worldDirectory, "level.dat");
            CompressedStreamTools.writeCompressed(nbttagcompound1, new FileOutputStream(file1));

            if (file2.exists()) {
                file2.delete();
            }

            file3.renameTo(file2);

            if (file3.exists()) {
                file3.delete();
            }

            file1.renameTo(file3);

            if (file1.exists()) {
                file1.delete();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void writePlayerData(EntityPlayer player) {
        try {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            player.writeToNBT(nbttagcompound);
            File file1 = new File(playersDirectory, player.getUniqueID().toString() + ".dat.tmp");
            File file2 = new File(playersDirectory, player.getUniqueID().toString() + ".dat");
            CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file1));

            if (file2.exists()) {
                file2.delete();
            }

            file1.renameTo(file2);
        } catch (Exception var5) {
            logger.warn("Failed to save player data for " + player.getName());
        }
    }

    public NBTTagCompound readPlayerData(EntityPlayer player) {
        NBTTagCompound nbttagcompound = null;

        try {
            File file1 = new File(playersDirectory, player.getUniqueID().toString() + ".dat");

            if (file1.exists() && file1.isFile()) {
                nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file1));
            }
        } catch (Exception var4) {
            logger.warn("Failed to load player data for " + player.getName());
        }

        if (nbttagcompound != null) {
            player.readFromNBT(nbttagcompound);
        }

        return nbttagcompound;
    }

    public IPlayerFileData getPlayerNBTManager() {
        return this;
    }

    public String[] getAvailablePlayerDat() {
        String[] astring = playersDirectory.list();

        if (astring == null) {
            astring = new String[0];
        }

        for (int i = 0; i < astring.length; ++i) {
            if (astring[i].endsWith(".dat")) {
                astring[i] = astring[i].substring(0, astring[i].length() - 4);
            }
        }

        return astring;
    }

    public void flush() {
    }

    public File getMapFileFromName(String mapName) {
        return new File(mapDataDir, mapName + ".dat");
    }

    public String getWorldDirectoryName() {
        return saveDirectoryName;
    }
}
