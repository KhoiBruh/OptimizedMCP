package net.minecraft.world.chunk.storage;

import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class RegionFile {
    private static final byte[] emptySector = new byte[4096];
    private final File fileName;
    private final int[] offsets = new int[1024];
    private final int[] chunkTimestamps = new int[1024];
    private RandomAccessFile dataFile;
    private List<Boolean> sectorFree;
    private int sizeDelta;
    private long lastModified;

    public RegionFile(File fileNameIn) {
        fileName = fileNameIn;
        sizeDelta = 0;

        try {
            if (fileNameIn.exists()) {
                lastModified = fileNameIn.lastModified();
            }

            dataFile = new RandomAccessFile(fileNameIn, "rw");

            if (dataFile.length() < 4096L) {
                for (int i = 0; i < 1024; ++i) {
                    dataFile.writeInt(0);
                }

                for (int i1 = 0; i1 < 1024; ++i1) {
                    dataFile.writeInt(0);
                }

                sizeDelta += 8192;
            }

            if ((dataFile.length() & 4095L) != 0L) {
                for (int j1 = 0; (long) j1 < (dataFile.length() & 4095L); ++j1) {
                    dataFile.write(0);
                }
            }

            int k1 = (int) dataFile.length() / 4096;
            sectorFree = Lists.newArrayListWithCapacity(k1);

            for (int j = 0; j < k1; ++j) {
                sectorFree.add(Boolean.TRUE);
            }

            sectorFree.set(0, Boolean.FALSE);
            sectorFree.set(1, Boolean.FALSE);
            dataFile.seek(0L);

            for (int l1 = 0; l1 < 1024; ++l1) {
                int k = dataFile.readInt();
                offsets[l1] = k;

                if (k != 0 && (k >> 8) + (k & 255) <= sectorFree.size()) {
                    for (int l = 0; l < (k & 255); ++l) {
                        sectorFree.set((k >> 8) + l, Boolean.FALSE);
                    }
                }
            }

            for (int i2 = 0; i2 < 1024; ++i2) {
                int j2 = dataFile.readInt();
                chunkTimestamps[i2] = j2;
            }
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    public synchronized DataInputStream getChunkDataInputStream(int x, int z) {
        if (outOfBounds(x, z)) {
            return null;
        } else {
            try {
                int i = getOffset(x, z);

                if (i == 0) {
                    return null;
                } else {
                    int j = i >> 8;
                    int k = i & 255;

                    if (j + k > sectorFree.size()) {
                        return null;
                    } else {
                        dataFile.seek((long) (j * 4096L));
                        int l = dataFile.readInt();

                        if (l > 4096 * k) {
                            return null;
                        } else if (l <= 0) {
                            return null;
                        } else {
                            byte b0 = dataFile.readByte();

                            if (b0 == 1) {
                                byte[] abyte1 = new byte[l - 1];
                                dataFile.read(abyte1);
                                return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(abyte1))));
                            } else if (b0 == 2) {
                                byte[] abyte = new byte[l - 1];
                                dataFile.read(abyte);
                                return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(abyte))));
                            } else {
                                return null;
                            }
                        }
                    }
                }
            } catch (IOException var9) {
                return null;
            }
        }
    }

    public DataOutputStream getChunkDataOutputStream(int x, int z) {
        return outOfBounds(x, z) ? null : new DataOutputStream(new DeflaterOutputStream(new RegionFile.ChunkBuffer(x, z)));
    }

    protected synchronized void write(int x, int z, byte[] data, int length) {
        try {
            int i = getOffset(x, z);
            int j = i >> 8;
            int k = i & 255;
            int l = (length + 5) / 4096 + 1;

            if (l >= 256) {
                return;
            }

            if (j != 0 && k == l) {
                write(j, data, length);
            } else {
                for (int i1 = 0; i1 < k; ++i1) {
                    sectorFree.set(j + i1, Boolean.TRUE);
                }

                int l1 = sectorFree.indexOf(Boolean.TRUE);
                int j1 = 0;

                if (l1 != -1) {
                    for (int k1 = l1; k1 < sectorFree.size(); ++k1) {
                        if (j1 != 0) {
                            if (sectorFree.get(k1)) {
                                ++j1;
                            } else {
                                j1 = 0;
                            }
                        } else if (sectorFree.get(k1)) {
                            l1 = k1;
                            j1 = 1;
                        }

                        if (j1 >= l) {
                            break;
                        }
                    }
                }

                if (j1 >= l) {
                    j = l1;
                    setOffset(x, z, l1 << 8 | l);

                    for (int j2 = 0; j2 < l; ++j2) {
                        sectorFree.set(j + j2, Boolean.FALSE);
                    }

                    write(j, data, length);
                } else {
                    dataFile.seek(dataFile.length());
                    j = sectorFree.size();

                    for (int i2 = 0; i2 < l; ++i2) {
                        dataFile.write(emptySector);
                        sectorFree.add(Boolean.FALSE);
                    }

                    sizeDelta += 4096 * l;
                    write(j, data, length);
                    setOffset(x, z, j << 8 | l);
                }
            }

            setChunkTimestamp(x, z, (int) (MinecraftServer.getCurrentTimeMillis() / 1000L));
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    private void write(int sectorNumber, byte[] data, int length) throws IOException {
        dataFile.seek((long) (sectorNumber * 4096L));
        dataFile.writeInt(length + 1);
        dataFile.writeByte(2);
        dataFile.write(data, 0, length);
    }

    private boolean outOfBounds(int x, int z) {
        return x < 0 || x >= 32 || z < 0 || z >= 32;
    }

    private int getOffset(int x, int z) {
        return offsets[x + z * 32];
    }

    public boolean isChunkSaved(int x, int z) {
        return getOffset(x, z) != 0;
    }

    private void setOffset(int x, int z, int offset) throws IOException {
        offsets[x + z * 32] = offset;
        dataFile.seek((long) ((x + z * 32L) * 4));
        dataFile.writeInt(offset);
    }

    private void setChunkTimestamp(int x, int z, int timestamp) throws IOException {
        chunkTimestamps[x + z * 32] = timestamp;
        dataFile.seek((long) (4096 + (x + z * 32L) * 4));
        dataFile.writeInt(timestamp);
    }

    public void close() throws IOException {
        if (dataFile != null) {
            dataFile.close();
        }
    }

    class ChunkBuffer extends ByteArrayOutputStream {
        private final int chunkX;
        private final int chunkZ;

        public ChunkBuffer(int x, int z) {
            super(8096);
            chunkX = x;
            chunkZ = z;
        }

        public void close() {
            RegionFile.this.write(chunkX, chunkZ, buf, count);
        }
    }
}
