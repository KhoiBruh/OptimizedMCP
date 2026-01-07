package net.minecraft.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Rotations;
import net.minecraft.world.biome.BiomeGenBase;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataWatcher {
    private static final Map<Class<?>, Integer> dataTypes = Maps.newHashMap();

    static {
        dataTypes.put(Byte.class, 0);
        dataTypes.put(Short.class, 1);
        dataTypes.put(Integer.class, 2);
        dataTypes.put(Float.class, 3);
        dataTypes.put(String.class, 4);
        dataTypes.put(ItemStack.class, 5);
        dataTypes.put(BlockPos.class, 6);
        dataTypes.put(Rotations.class, 7);
    }

    private final Entity owner;
    private final Map<Integer, DataWatcher.WatchableObject> watchedObjects = Maps.newHashMap();
    public BiomeGenBase spawnBiome = BiomeGenBase.plains;
    public BlockPos spawnPosition = BlockPos.ORIGIN;
    private boolean isBlank = true;
    private boolean objectChanged;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public DataWatcher(Entity owner) {
        this.owner = owner;
    }

    public static void writeWatchedListToPacketBuffer(List<DataWatcher.WatchableObject> objectsList, PacketBuffer buffer) throws IOException {
        if (objectsList != null) {
            for (DataWatcher.WatchableObject datawatcher$watchableobject : objectsList) {
                writeWatchableObjectToPacketBuffer(buffer, datawatcher$watchableobject);
            }
        }

        buffer.writeByte(127);
    }

    private static void writeWatchableObjectToPacketBuffer(PacketBuffer buffer, DataWatcher.WatchableObject object) throws IOException {
        int i = (object.getObjectType() << 5 | object.getDataValueId() & 31) & 255;
        buffer.writeByte(i);

        switch (object.getObjectType()) {
            case 0:
                buffer.writeByte((Byte) object.getObject());
                break;

            case 1:
                buffer.writeShort((Short) object.getObject());
                break;

            case 2:
                buffer.writeInt((Integer) object.getObject());
                break;

            case 3:
                buffer.writeFloat((Float) object.getObject());
                break;

            case 4:
                buffer.writeString((String) object.getObject());
                break;

            case 5:
                ItemStack itemstack = (ItemStack) object.getObject();
                buffer.writeItemStackToBuffer(itemstack);
                break;

            case 6:
                BlockPos blockpos = (BlockPos) object.getObject();
                buffer.writeInt(blockpos.getX());
                buffer.writeInt(blockpos.getY());
                buffer.writeInt(blockpos.getZ());
                break;

            case 7:
                Rotations rotations = (Rotations) object.getObject();
                buffer.writeFloat(rotations.getX());
                buffer.writeFloat(rotations.getY());
                buffer.writeFloat(rotations.getZ());
        }
    }

    public static List<DataWatcher.WatchableObject> readWatchedListFromPacketBuffer(PacketBuffer buffer) throws IOException {
        List<DataWatcher.WatchableObject> list = null;

        for (int i = buffer.readByte(); i != 127; i = buffer.readByte()) {
            if (list == null) {
                list = Lists.newArrayList();
            }

            int j = (i & 224) >> 5;
            int k = i & 31;
            DataWatcher.WatchableObject datawatcher$watchableobject = null;

            switch (j) {
                case 0:
                    datawatcher$watchableobject = new DataWatcher.WatchableObject(j, k, buffer.readByte());
                    break;

                case 1:
                    datawatcher$watchableobject = new DataWatcher.WatchableObject(j, k, buffer.readShort());
                    break;

                case 2:
                    datawatcher$watchableobject = new DataWatcher.WatchableObject(j, k, buffer.readInt());
                    break;

                case 3:
                    datawatcher$watchableobject = new DataWatcher.WatchableObject(j, k, buffer.readFloat());
                    break;

                case 4:
                    datawatcher$watchableobject = new DataWatcher.WatchableObject(j, k, buffer.readStringFromBuffer(32767));
                    break;

                case 5:
                    datawatcher$watchableobject = new DataWatcher.WatchableObject(j, k, buffer.readItemStackFromBuffer());
                    break;

                case 6:
                    int l = buffer.readInt();
                    int i1 = buffer.readInt();
                    int j1 = buffer.readInt();
                    datawatcher$watchableobject = new DataWatcher.WatchableObject(j, k, new BlockPos(l, i1, j1));
                    break;

                case 7:
                    float f = buffer.readFloat();
                    float f1 = buffer.readFloat();
                    float f2 = buffer.readFloat();
                    datawatcher$watchableobject = new DataWatcher.WatchableObject(j, k, new Rotations(f, f1, f2));
            }

            list.add(datawatcher$watchableobject);
        }

        return list;
    }

    public <T> void addObject(int id, T object) {
        Integer integer = dataTypes.get(object.getClass());

        if (integer == null) {
            throw new IllegalArgumentException("Unknown data type: " + object.getClass());
        } else if (id > 31) {
            throw new IllegalArgumentException("Data value id is too big with " + id + "! (Max is " + 31 + ")");
        } else if (watchedObjects.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate id value for " + id + "!");
        } else {
            DataWatcher.WatchableObject datawatcher$watchableobject = new DataWatcher.WatchableObject(integer, id, object);
            lock.writeLock().lock();
            watchedObjects.put(id, datawatcher$watchableobject);
            lock.writeLock().unlock();
            isBlank = false;
        }
    }

    public void addObjectByDataType(int id, int type) {
        DataWatcher.WatchableObject datawatcher$watchableobject = new DataWatcher.WatchableObject(type, id, null);
        lock.writeLock().lock();
        watchedObjects.put(id, datawatcher$watchableobject);
        lock.writeLock().unlock();
        isBlank = false;
    }

    public byte getWatchableObjectByte(int id) {
        return (Byte) getWatchedObject(id).getObject();
    }

    public short getWatchableObjectShort(int id) {
        return (Short) getWatchedObject(id).getObject();
    }

    public int getWatchableObjectInt(int id) {
        return (Integer) getWatchedObject(id).getObject();
    }

    public float getWatchableObjectFloat(int id) {
        return (Float) getWatchedObject(id).getObject();
    }

    public String getWatchableObjectString(int id) {
        return (String) getWatchedObject(id).getObject();
    }

    public ItemStack getWatchableObjectItemStack(int id) {
        return (ItemStack) getWatchedObject(id).getObject();
    }

    private DataWatcher.WatchableObject getWatchedObject(int id) {
        lock.readLock().lock();
        DataWatcher.WatchableObject datawatcher$watchableobject;

        try {
            datawatcher$watchableobject = watchedObjects.get(id);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting synched entity data");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Synched entity data");
            crashreportcategory.addCrashSection("Data ID", id);
            throw new ReportedException(crashreport);
        }

        lock.readLock().unlock();
        return datawatcher$watchableobject;
    }

    public Rotations getWatchableObjectRotations(int id) {
        return (Rotations) getWatchedObject(id).getObject();
    }

    public <T> void updateObject(int id, T newData) {
        DataWatcher.WatchableObject datawatcher$watchableobject = getWatchedObject(id);

        if (ObjectUtils.notEqual(newData, datawatcher$watchableobject.getObject())) {
            datawatcher$watchableobject.setObject(newData);
            owner.onDataWatcherUpdate(id);
            datawatcher$watchableobject.setWatched(true);
            objectChanged = true;
        }
    }

    public void setObjectWatched(int id) {
        getWatchedObject(id).watched = true;
        objectChanged = true;
    }

    public boolean hasObjectChanged() {
        return objectChanged;
    }

    public List<DataWatcher.WatchableObject> getChanged() {
        List<DataWatcher.WatchableObject> list = null;

        if (objectChanged) {
            lock.readLock().lock();

            for (DataWatcher.WatchableObject datawatcher$watchableobject : watchedObjects.values()) {
                if (datawatcher$watchableobject.isWatched()) {
                    datawatcher$watchableobject.setWatched(false);

                    if (list == null) {
                        list = Lists.newArrayList();
                    }

                    list.add(datawatcher$watchableobject);
                }
            }

            lock.readLock().unlock();
        }

        objectChanged = false;
        return list;
    }

    public void writeTo(PacketBuffer buffer) throws IOException {
        lock.readLock().lock();

        for (DataWatcher.WatchableObject datawatcher$watchableobject : watchedObjects.values()) {
            writeWatchableObjectToPacketBuffer(buffer, datawatcher$watchableobject);
        }

        lock.readLock().unlock();
        buffer.writeByte(127);
    }

    public List<DataWatcher.WatchableObject> getAllWatched() {
        List<DataWatcher.WatchableObject> list = null;
        lock.readLock().lock();

        for (DataWatcher.WatchableObject datawatcher$watchableobject : watchedObjects.values()) {
            if (list == null) {
                list = Lists.newArrayList();
            }

            list.add(datawatcher$watchableobject);
        }

        lock.readLock().unlock();
        return list;
    }

    public void updateWatchedObjectsFromList(List<DataWatcher.WatchableObject> p_75687_1_) {
        lock.writeLock().lock();

        for (DataWatcher.WatchableObject datawatcher$watchableobject : p_75687_1_) {
            DataWatcher.WatchableObject datawatcher$watchableobject1 = watchedObjects.get(datawatcher$watchableobject.getDataValueId());

            if (datawatcher$watchableobject1 != null) {
                datawatcher$watchableobject1.setObject(datawatcher$watchableobject.getObject());
                owner.onDataWatcherUpdate(datawatcher$watchableobject.getDataValueId());
            }
        }

        lock.writeLock().unlock();
        objectChanged = true;
    }

    public boolean getIsBlank() {
        return isBlank;
    }

    public void func_111144_e() {
        objectChanged = false;
    }

    public static class WatchableObject {
        private final int objectType;
        private final int dataValueId;
        private Object watchedObject;
        private boolean watched;

        public WatchableObject(int type, int id, Object object) {
            dataValueId = id;
            watchedObject = object;
            objectType = type;
            watched = true;
        }

        public int getDataValueId() {
            return dataValueId;
        }

        public Object getObject() {
            return watchedObject;
        }

        public void setObject(Object object) {
            watchedObject = object;
        }

        public int getObjectType() {
            return objectType;
        }

        public boolean isWatched() {
            return watched;
        }

        public void setWatched(boolean watched) {
            this.watched = watched;
        }
    }
}
