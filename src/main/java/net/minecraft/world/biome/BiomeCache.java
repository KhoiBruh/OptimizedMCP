package net.minecraft.world.biome;

import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.LongHashMap;

import java.util.List;

public class BiomeCache {
    private final WorldChunkManager chunkManager;
    private long lastCleanupTime;
    private final LongHashMap<BiomeCache.Block> cacheMap = new LongHashMap();
    private final List<BiomeCache.Block> cache = Lists.newArrayList();

    public BiomeCache(WorldChunkManager chunkManagerIn) {
        chunkManager = chunkManagerIn;
    }

    public BiomeCache.Block getBiomeCacheBlock(int x, int z) {
        x = x >> 4;
        z = z >> 4;
        long i = (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
        BiomeCache.Block biomecache$block = cacheMap.getValueByKey(i);

        if (biomecache$block == null) {
            biomecache$block = new BiomeCache.Block(x, z);
            cacheMap.add(i, biomecache$block);
            cache.add(biomecache$block);
        }

        biomecache$block.lastAccessTime = MinecraftServer.getCurrentTimeMillis();
        return biomecache$block;
    }

    public BiomeGenBase func_180284_a(int x, int z, BiomeGenBase p_180284_3_) {
        BiomeGenBase biomegenbase = getBiomeCacheBlock(x, z).getBiomeGenAt(x, z);
        return biomegenbase == null ? p_180284_3_ : biomegenbase;
    }

    public void cleanupCache() {
        long i = MinecraftServer.getCurrentTimeMillis();
        long j = i - lastCleanupTime;

        if (j > 7500L || j < 0L) {
            lastCleanupTime = i;

            for (int k = 0; k < cache.size(); ++k) {
                BiomeCache.Block biomecache$block = cache.get(k);
                long l = i - biomecache$block.lastAccessTime;

                if (l > 30000L || l < 0L) {
                    cache.remove(k--);
                    long i1 = (long) biomecache$block.xPosition & 4294967295L | ((long) biomecache$block.zPosition & 4294967295L) << 32;
                    cacheMap.remove(i1);
                }
            }
        }
    }

    public BiomeGenBase[] getCachedBiomes(int x, int z) {
        return getBiomeCacheBlock(x, z).biomes;
    }

    public class Block {
        public float[] rainfallValues = new float[256];
        public BiomeGenBase[] biomes = new BiomeGenBase[256];
        public int xPosition;
        public int zPosition;
        public long lastAccessTime;

        public Block(int x, int z) {
            xPosition = x;
            zPosition = z;
            chunkManager.getRainfall(rainfallValues, x << 4, z << 4, 16, 16);
            chunkManager.getBiomeGenAt(biomes, x << 4, z << 4, 16, 16, false);
        }

        public BiomeGenBase getBiomeGenAt(int x, int z) {
            return biomes[x & 15 | (z & 15) << 4];
        }
    }
}
