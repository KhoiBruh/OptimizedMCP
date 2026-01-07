package net.minecraft.server.management;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.optifine.ChunkPosComparator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;

public class PlayerManager {
    private static final Logger pmLogger = LogManager.getLogger();
    private final WorldServer theWorldServer;
    private final List<EntityPlayerMP> players = Lists.newArrayList();
    private final LongHashMap<PlayerManager.PlayerInstance> playerInstances = new LongHashMap<>();
    private final List<PlayerManager.PlayerInstance> playerInstancesToUpdate = Lists.newArrayList();
    private final List<PlayerManager.PlayerInstance> playerInstanceList = Lists.newArrayList();
    private final int[][] xzDirectionsConst = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
    private final Map<EntityPlayerMP, Set<ChunkCoordIntPair>> mapPlayerPendingEntries = new HashMap<>();
    private int playerViewRadius;
    private long previousTotalWorldTime;

    public PlayerManager(WorldServer serverWorld) {
        theWorldServer = serverWorld;
        setPlayerViewRadius(serverWorld.getMinecraftServer().getConfigurationManager().getViewDistance());
    }

    public static int getFurthestViewableBlock(int distance) {
        return distance * 16 - 16;
    }

    public WorldServer getWorldServer() {
        return theWorldServer;
    }

    public void updatePlayerInstances() {
        Set<Entry<EntityPlayerMP, Set<ChunkCoordIntPair>>> set = mapPlayerPendingEntries.entrySet();
        Iterator iterator = set.iterator();

        while (iterator.hasNext()) {
            Entry<EntityPlayerMP, Set<ChunkCoordIntPair>> entry = (Entry) iterator.next();
            Set<ChunkCoordIntPair> set1 = entry.getValue();

            if (!set1.isEmpty()) {
                EntityPlayerMP entityplayermp = entry.getKey();

                if (entityplayermp.worldObj != theWorldServer) {
                    iterator.remove();
                } else {
                    int i = playerViewRadius / 3 + 1;

                    if (!Config.isLazyChunkLoading()) {
                        i = playerViewRadius * 2 + 1;
                    }

                    for (ChunkCoordIntPair chunkcoordintpair : getNearest(set1, entityplayermp, i)) {
                        PlayerManager.PlayerInstance playermanager$playerinstance = getPlayerInstance(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos, true);
                        playermanager$playerinstance.addPlayer(entityplayermp);
                        set1.remove(chunkcoordintpair);
                    }
                }
            }
        }

        long j = theWorldServer.getTotalWorldTime();

        if (j - previousTotalWorldTime > 8000L) {
            previousTotalWorldTime = j;

            for (PlayerInstance playermanager$playerinstance1 : playerInstanceList) {
                playermanager$playerinstance1.onUpdate();
                playermanager$playerinstance1.processChunk();
            }
        } else {
            for (PlayerInstance playermanager$playerinstance2 : playerInstancesToUpdate) {
                playermanager$playerinstance2.onUpdate();
            }
        }

        playerInstancesToUpdate.clear();

        if (players.isEmpty()) {
            WorldProvider worldprovider = theWorldServer.provider;

            if (!worldprovider.canRespawnHere()) {
                theWorldServer.theChunkProviderServer.unloadAllChunks();
            }
        }
    }

    public boolean hasPlayerInstance(int chunkX, int chunkZ) {
        long i = (long) chunkX + 2147483647L | (long) chunkZ + 2147483647L << 32;
        return playerInstances.getValueByKey(i) != null;
    }

    private PlayerManager.PlayerInstance getPlayerInstance(int chunkX, int chunkZ, boolean createIfAbsent) {
        long i = (long) chunkX + 2147483647L | (long) chunkZ + 2147483647L << 32;
        PlayerManager.PlayerInstance playermanager$playerinstance = playerInstances.getValueByKey(i);

        if (playermanager$playerinstance == null && createIfAbsent) {
            playermanager$playerinstance = new PlayerManager.PlayerInstance(chunkX, chunkZ);
            playerInstances.add(i, playermanager$playerinstance);
            playerInstanceList.add(playermanager$playerinstance);
        }

        return playermanager$playerinstance;
    }

    public void markBlockForUpdate(BlockPos pos) {
        int i = pos.getX() >> 4;
        int j = pos.getZ() >> 4;
        PlayerManager.PlayerInstance playermanager$playerinstance = getPlayerInstance(i, j, false);

        if (playermanager$playerinstance != null) {
            playermanager$playerinstance.flagChunkForUpdate(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
        }
    }

    public void addPlayer(EntityPlayerMP player) {
        int i = (int) player.posX >> 4;
        int j = (int) player.posZ >> 4;
        player.managedPosX = player.posX;
        player.managedPosZ = player.posZ;
        int k = Math.min(playerViewRadius, 8);
        int l = i - k;
        int i1 = i + k;
        int j1 = j - k;
        int k1 = j + k;
        Set<ChunkCoordIntPair> set = getPendingEntriesSafe(player);

        for (int l1 = i - playerViewRadius; l1 <= i + playerViewRadius; ++l1) {
            for (int i2 = j - playerViewRadius; i2 <= j + playerViewRadius; ++i2) {
                if (l1 >= l && l1 <= i1 && i2 >= j1 && i2 <= k1) {
                    getPlayerInstance(l1, i2, true).addPlayer(player);
                } else {
                    set.add(new ChunkCoordIntPair(l1, i2));
                }
            }
        }

        players.add(player);
        filterChunkLoadQueue(player);
    }

    public void filterChunkLoadQueue(EntityPlayerMP player) {
        List<ChunkCoordIntPair> list = Lists.newArrayList(player.loadedChunks);
        int i = 0;
        int j = playerViewRadius;
        int k = (int) player.posX >> 4;
        int l = (int) player.posZ >> 4;
        int i1 = 0;
        int j1 = 0;
        ChunkCoordIntPair chunkcoordintpair = getPlayerInstance(k, l, true).chunkCoords;
        player.loadedChunks.clear();

        if (list.contains(chunkcoordintpair)) {
            player.loadedChunks.add(chunkcoordintpair);
        }

        for (int k1 = 1; k1 <= j * 2; ++k1) {
            for (int l1 = 0; l1 < 2; ++l1) {
                int[] aint = xzDirectionsConst[i++ % 4];

                for (int i2 = 0; i2 < k1; ++i2) {
                    i1 += aint[0];
                    j1 += aint[1];
                    chunkcoordintpair = getPlayerInstance(k + i1, l + j1, true).chunkCoords;

                    if (list.contains(chunkcoordintpair)) {
                        player.loadedChunks.add(chunkcoordintpair);
                    }
                }
            }
        }

        i = i % 4;

        for (int j2 = 0; j2 < j * 2; ++j2) {
            i1 += xzDirectionsConst[i][0];
            j1 += xzDirectionsConst[i][1];
            chunkcoordintpair = getPlayerInstance(k + i1, l + j1, true).chunkCoords;

            if (list.contains(chunkcoordintpair)) {
                player.loadedChunks.add(chunkcoordintpair);
            }
        }
    }

    public void removePlayer(EntityPlayerMP player) {
        mapPlayerPendingEntries.remove(player);
        int i = (int) player.managedPosX >> 4;
        int j = (int) player.managedPosZ >> 4;

        for (int k = i - playerViewRadius; k <= i + playerViewRadius; ++k) {
            for (int l = j - playerViewRadius; l <= j + playerViewRadius; ++l) {
                PlayerManager.PlayerInstance playermanager$playerinstance = getPlayerInstance(k, l, false);

                if (playermanager$playerinstance != null) {
                    playermanager$playerinstance.removePlayer(player);
                }
            }
        }

        players.remove(player);
    }

    private boolean overlaps(int x1, int z1, int x2, int z2, int radius) {
        int i = x1 - x2;
        int j = z1 - z2;
        return i >= -radius && i <= radius && j >= -radius && j <= radius;
    }

    public void updateMountedMovingPlayer(EntityPlayerMP player) {
        int i = (int) player.posX >> 4;
        int j = (int) player.posZ >> 4;
        double d0 = player.managedPosX - player.posX;
        double d1 = player.managedPosZ - player.posZ;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 >= 64.0D) {
            int k = (int) player.managedPosX >> 4;
            int l = (int) player.managedPosZ >> 4;
            int i1 = playerViewRadius;
            int j1 = i - k;
            int k1 = j - l;

            if (j1 != 0 || k1 != 0) {
                Set<ChunkCoordIntPair> set = getPendingEntriesSafe(player);

                for (int l1 = i - i1; l1 <= i + i1; ++l1) {
                    for (int i2 = j - i1; i2 <= j + i1; ++i2) {
                        if (!overlaps(l1, i2, k, l, i1)) {
                            if (Config.isLazyChunkLoading()) {
                                set.add(new ChunkCoordIntPair(l1, i2));
                            } else {
                                getPlayerInstance(l1, i2, true).addPlayer(player);
                            }
                        }

                        if (!overlaps(l1 - j1, i2 - k1, i, j, i1)) {
                            set.remove(new ChunkCoordIntPair(l1 - j1, i2 - k1));
                            PlayerManager.PlayerInstance playermanager$playerinstance = getPlayerInstance(l1 - j1, i2 - k1, false);

                            if (playermanager$playerinstance != null) {
                                playermanager$playerinstance.removePlayer(player);
                            }
                        }
                    }
                }

                filterChunkLoadQueue(player);
                player.managedPosX = player.posX;
                player.managedPosZ = player.posZ;
            }
        }
    }

    public boolean isPlayerWatchingChunk(EntityPlayerMP player, int chunkX, int chunkZ) {
        PlayerManager.PlayerInstance playermanager$playerinstance = getPlayerInstance(chunkX, chunkZ, false);
        return playermanager$playerinstance != null && playermanager$playerinstance.playersWatchingChunk.contains(player) && !player.loadedChunks.contains(playermanager$playerinstance.chunkCoords);
    }

    public void setPlayerViewRadius(int radius) {
        radius = MathHelper.clamp_int(radius, 3, 64);

        if (radius != playerViewRadius) {
            int i = radius - playerViewRadius;

            for (EntityPlayerMP entityplayermp : Lists.newArrayList(players)) {
                int j = (int) entityplayermp.posX >> 4;
                int k = (int) entityplayermp.posZ >> 4;
                Set<ChunkCoordIntPair> set = getPendingEntriesSafe(entityplayermp);

                if (i > 0) {
                    for (int j1 = j - radius; j1 <= j + radius; ++j1) {
                        for (int k1 = k - radius; k1 <= k + radius; ++k1) {
                            if (Config.isLazyChunkLoading()) {
                                set.add(new ChunkCoordIntPair(j1, k1));
                            } else {
                                PlayerManager.PlayerInstance playermanager$playerinstance1 = getPlayerInstance(j1, k1, true);

                                if (!playermanager$playerinstance1.playersWatchingChunk.contains(entityplayermp)) {
                                    playermanager$playerinstance1.addPlayer(entityplayermp);
                                }
                            }
                        }
                    }
                } else {
                    for (int l = j - playerViewRadius; l <= j + playerViewRadius; ++l) {
                        for (int i1 = k - playerViewRadius; i1 <= k + playerViewRadius; ++i1) {
                            if (!overlaps(l, i1, j, k, radius)) {
                                set.remove(new ChunkCoordIntPair(l, i1));
                                PlayerManager.PlayerInstance playermanager$playerinstance = getPlayerInstance(l, i1, true);

                                if (playermanager$playerinstance != null) {
                                    playermanager$playerinstance.removePlayer(entityplayermp);
                                }
                            }
                        }
                    }
                }
            }

            playerViewRadius = radius;
        }
    }

    private PriorityQueue<ChunkCoordIntPair> getNearest(Set<ChunkCoordIntPair> p_getNearest_1_, EntityPlayerMP p_getNearest_2_, int p_getNearest_3_) {
        float f;

        for (f = p_getNearest_2_.rotationYaw + 90.0F; f <= -180.0F; f += 360.0F) {
        }

        while (f > 180.0F) {
            f -= 360.0F;
        }

        double d0 = (double) f * 0.017453292519943295D;
        double d1 = p_getNearest_2_.rotationPitch;
        double d2 = d1 * 0.017453292519943295D;
        ChunkPosComparator chunkposcomparator = new ChunkPosComparator(p_getNearest_2_.chunkCoordX, p_getNearest_2_.chunkCoordZ, d0, d2);
        Comparator<ChunkCoordIntPair> comparator = Collections.reverseOrder(chunkposcomparator);
        PriorityQueue<ChunkCoordIntPair> priorityqueue = new PriorityQueue<>(p_getNearest_3_, comparator);

        for (ChunkCoordIntPair chunkcoordintpair : p_getNearest_1_) {
            if (priorityqueue.size() < p_getNearest_3_) {
                priorityqueue.add(chunkcoordintpair);
            } else {
                ChunkCoordIntPair chunkcoordintpair1 = priorityqueue.peek();

                if (chunkposcomparator.compare(chunkcoordintpair, chunkcoordintpair1) < 0) {
                    priorityqueue.remove();
                    priorityqueue.add(chunkcoordintpair);
                }
            }
        }

        return priorityqueue;
    }

    private Set<ChunkCoordIntPair> getPendingEntriesSafe(EntityPlayerMP p_getPendingEntriesSafe_1_) {
        Set<ChunkCoordIntPair> set = mapPlayerPendingEntries.get(p_getPendingEntriesSafe_1_);

        if (set != null) {
            return set;
        } else {
            int i = Math.min(playerViewRadius, 8);
            int j = playerViewRadius * 2 + 1;
            int k = i * 2 + 1;
            int l = j * j - k * k;
            l = Math.max(l, 16);
            HashSet hashset = new HashSet(l);
            mapPlayerPendingEntries.put(p_getPendingEntriesSafe_1_, hashset);
            return hashset;
        }
    }

    class PlayerInstance {
        private final List<EntityPlayerMP> playersWatchingChunk = Lists.newArrayList();
        private final ChunkCoordIntPair chunkCoords;
        private final short[] locationOfBlockChange = new short[64];
        private int numBlocksToUpdate;
        private int flagsYAreasToUpdate;
        private long previousWorldTime;

        public PlayerInstance(int chunkX, int chunkZ) {
            chunkCoords = new ChunkCoordIntPair(chunkX, chunkZ);
            getWorldServer().theChunkProviderServer.loadChunk(chunkX, chunkZ);
        }

        public void addPlayer(EntityPlayerMP player) {
            if (playersWatchingChunk.contains(player)) {
                PlayerManager.pmLogger.debug("Failed to add player. {} already is in chunk {}, {}", new Object[]{player, chunkCoords.chunkXPos, chunkCoords.chunkZPos});
            } else {
                if (playersWatchingChunk.isEmpty()) {
                    previousWorldTime = theWorldServer.getTotalWorldTime();
                }

                playersWatchingChunk.add(player);
                player.loadedChunks.add(chunkCoords);
            }
        }

        public void removePlayer(EntityPlayerMP player) {
            if (playersWatchingChunk.contains(player)) {
                Chunk chunk = theWorldServer.getChunkFromChunkCoords(chunkCoords.chunkXPos, chunkCoords.chunkZPos);

                if (chunk.isPopulated()) {
                    player.playerNetServerHandler.sendPacket(new S21PacketChunkData(chunk, true, 0));
                }

                playersWatchingChunk.remove(player);
                player.loadedChunks.remove(chunkCoords);

                if (playersWatchingChunk.isEmpty()) {
                    long i = (long) chunkCoords.chunkXPos + 2147483647L | (long) chunkCoords.chunkZPos + 2147483647L << 32;
                    increaseInhabitedTime(chunk);
                    playerInstances.remove(i);
                    playerInstanceList.remove(this);

                    if (numBlocksToUpdate > 0) {
                        playerInstancesToUpdate.remove(this);
                    }

                    getWorldServer().theChunkProviderServer.dropChunk(chunkCoords.chunkXPos, chunkCoords.chunkZPos);
                }
            }
        }

        public void processChunk() {
            increaseInhabitedTime(theWorldServer.getChunkFromChunkCoords(chunkCoords.chunkXPos, chunkCoords.chunkZPos));
        }

        private void increaseInhabitedTime(Chunk theChunk) {
            theChunk.setInhabitedTime(theChunk.getInhabitedTime() + theWorldServer.getTotalWorldTime() - previousWorldTime);
            previousWorldTime = theWorldServer.getTotalWorldTime();
        }

        public void flagChunkForUpdate(int x, int y, int z) {
            if (numBlocksToUpdate == 0) {
                playerInstancesToUpdate.add(this);
            }

            flagsYAreasToUpdate |= 1 << (y >> 4);

            if (numBlocksToUpdate < 64) {
                short short1 = (short) (x << 12 | z << 8 | y);

                for (int i = 0; i < numBlocksToUpdate; ++i) {
                    if (locationOfBlockChange[i] == short1) {
                        return;
                    }
                }

                locationOfBlockChange[numBlocksToUpdate++] = short1;
            }
        }

        public void sendToAllPlayersWatchingChunk(Packet thePacket) {
            for (EntityPlayerMP entityplayermp : playersWatchingChunk) {
                if (!entityplayermp.loadedChunks.contains(chunkCoords)) {
                    entityplayermp.playerNetServerHandler.sendPacket(thePacket);
                }
            }
        }

        public void onUpdate() {
            if (numBlocksToUpdate != 0) {
                if (numBlocksToUpdate == 1) {
                    int k1 = (locationOfBlockChange[0] >> 12 & 15) + chunkCoords.chunkXPos * 16;
                    int i2 = locationOfBlockChange[0] & 255;
                    int k2 = (locationOfBlockChange[0] >> 8 & 15) + chunkCoords.chunkZPos * 16;
                    BlockPos blockpos = new BlockPos(k1, i2, k2);
                    sendToAllPlayersWatchingChunk(new S23PacketBlockChange(theWorldServer, blockpos));

                    if (theWorldServer.getBlockState(blockpos).getBlock().hasTileEntity()) {
                        sendTileToAllPlayersWatchingChunk(theWorldServer.getTileEntity(blockpos));
                    }
                } else if (numBlocksToUpdate != 64) {
                    sendToAllPlayersWatchingChunk(new S22PacketMultiBlockChange(numBlocksToUpdate, locationOfBlockChange, theWorldServer.getChunkFromChunkCoords(chunkCoords.chunkXPos, chunkCoords.chunkZPos)));

                    for (int j1 = 0; j1 < numBlocksToUpdate; ++j1) {
                        int l1 = (locationOfBlockChange[j1] >> 12 & 15) + chunkCoords.chunkXPos * 16;
                        int j2 = locationOfBlockChange[j1] & 255;
                        int l2 = (locationOfBlockChange[j1] >> 8 & 15) + chunkCoords.chunkZPos * 16;
                        BlockPos blockpos1 = new BlockPos(l1, j2, l2);

                        if (theWorldServer.getBlockState(blockpos1).getBlock().hasTileEntity()) {
                            sendTileToAllPlayersWatchingChunk(theWorldServer.getTileEntity(blockpos1));
                        }
                    }
                } else {
                    int i = chunkCoords.chunkXPos * 16;
                    int j = chunkCoords.chunkZPos * 16;
                    sendToAllPlayersWatchingChunk(new S21PacketChunkData(theWorldServer.getChunkFromChunkCoords(chunkCoords.chunkXPos, chunkCoords.chunkZPos), false, flagsYAreasToUpdate));

                    for (int k = 0; k < 16; ++k) {
                        if ((flagsYAreasToUpdate & 1 << k) != 0) {
                            int l = k << 4;
                            List<TileEntity> list = theWorldServer.getTileEntitiesIn(i, l, j, i + 16, l + 16, j + 16);

                            for (TileEntity tileEntity : list) {
                                sendTileToAllPlayersWatchingChunk(tileEntity);
                            }
                        }
                    }
                }

                numBlocksToUpdate = 0;
                flagsYAreasToUpdate = 0;
            }
        }

        private void sendTileToAllPlayersWatchingChunk(TileEntity theTileEntity) {
            if (theTileEntity != null) {
                Packet packet = theTileEntity.getDescriptionPacket();

                if (packet != null) {
                    sendToAllPlayersWatchingChunk(packet);
                }
            }
        }
    }
}
