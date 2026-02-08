package net.minecraft.client.multiplayer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecart;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EntityFirework;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveDataMemoryStorage;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.optifine.DynamicLights;
import net.optifine.override.PlayerControllerOF;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class WorldClient extends World {
    private final Set<Entity> entityList = new HashSet<>();
    private final Set<Entity> entitySpawnQueue = new HashSet<>();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Set<ChunkCoordIntPair> previousActiveChunkSet = new HashSet<>();
    private final NetHandlerPlayClient sendQueue;
    private ChunkProviderClient clientChunkProvider;
    private boolean playerUpdate = false;

    public WorldClient(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, EnumDifficulty difficulty, Profiler profilerIn) {
        super(new SaveHandlerMP(), new WorldInfo(settings, "MpServer"), WorldProvider.getProviderForDimension(dimension), profilerIn, true);
        sendQueue = netHandler;
        getWorldInfo().setDifficulty(difficulty);
        provider.registerWorld(this);
        setSpawnPoint(new BlockPos(8, 64, 8));
        chunkProvider = createChunkProvider();
        mapStorage = new SaveDataMemoryStorage();
        calculateInitialSkylight();
        calculateInitialWeather();

        if (mc.playerController != null && mc.playerController.getClass() == PlayerControllerMP.class) {
            mc.playerController = new PlayerControllerOF(mc, netHandler);
        }
    }

    public void tick() {
        super.tick();
        setTotalWorldTime(getTotalWorldTime() + 1L);

        if (getGameRules().getBoolean("doDaylightCycle")) {
            setWorldTime(getWorldTime() + 1L);
        }

        theProfiler.startSection("reEntryProcessing");

        for (int i = 0; i < 10 && !entitySpawnQueue.isEmpty(); ++i) {
            Entity entity = entitySpawnQueue.iterator().next();
            entitySpawnQueue.remove(entity);

            if (!loadedEntityList.contains(entity)) {
                spawnEntityInWorld(entity);
            }
        }

        theProfiler.endStartSection("chunkCache");
        clientChunkProvider.unloadQueuedChunks();
        theProfiler.endStartSection("blocks");
        updateBlocks();
        theProfiler.endSection();
    }

    public void invalidateBlockReceiveRegion(int x1, int y1, int z1, int x2, int y2, int z2) {
    }

    protected IChunkProvider createChunkProvider() {
        clientChunkProvider = new ChunkProviderClient(this);
        return clientChunkProvider;
    }

    protected void updateBlocks() {
        super.updateBlocks();
        previousActiveChunkSet.retainAll(activeChunkSet);

        if (previousActiveChunkSet.size() == activeChunkSet.size()) {
            previousActiveChunkSet.clear();
        }

        int i = 0;

        for (ChunkCoordIntPair chunkcoordintpair : activeChunkSet) {
            if (!previousActiveChunkSet.contains(chunkcoordintpair)) {
                int j = chunkcoordintpair.chunkXPos * 16;
                int k = chunkcoordintpair.chunkZPos * 16;
                theProfiler.startSection("getChunk");
                Chunk chunk = getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);
                playMoodSoundAndCheckLight(j, k, chunk);
                theProfiler.endSection();
                previousActiveChunkSet.add(chunkcoordintpair);
                ++i;

                if (i >= 10) {
                    return;
                }
            }
        }
    }

    public void doPreChunk(int chuncX, int chuncZ, boolean loadChunk) {
        if (loadChunk) {
            clientChunkProvider.loadChunk(chuncX, chuncZ);
        } else {
            clientChunkProvider.unloadChunk(chuncX, chuncZ);
        }

        if (!loadChunk) {
            markBlockRangeForRenderUpdate(chuncX * 16, 0, chuncZ * 16, chuncX * 16 + 15, 256, chuncZ * 16 + 15);
        }
    }

    public boolean spawnEntityInWorld(Entity entityIn) {
        boolean flag = super.spawnEntityInWorld(entityIn);
        entityList.add(entityIn);

        if (!flag) {
            entitySpawnQueue.add(entityIn);
        } else if (entityIn instanceof EntityMinecart) {
            mc.getSoundHandler().playSound(new MovingSoundMinecart((EntityMinecart) entityIn));
        }

        return flag;
    }

    public void removeEntity(Entity entityIn) {
        super.removeEntity(entityIn);
        entityList.remove(entityIn);
    }

    protected void onEntityAdded(Entity entityIn) {
        super.onEntityAdded(entityIn);

        entitySpawnQueue.remove(entityIn);
    }

    protected void onEntityRemoved(Entity entityIn) {
        super.onEntityRemoved(entityIn);
        boolean flag = false;

        if (entityList.contains(entityIn)) {
            if (entityIn.isEntityAlive()) {
                entitySpawnQueue.add(entityIn);
            } else {
                entityList.remove(entityIn);
            }
        }
    }

    public void addEntityToWorld(int entityID, Entity entityToSpawn) {
        Entity entity = getEntityByID(entityID);

        if (entity != null) {
            removeEntity(entity);
        }

        entityList.add(entityToSpawn);
        entityToSpawn.setEntityId(entityID);

        if (!spawnEntityInWorld(entityToSpawn)) {
            entitySpawnQueue.add(entityToSpawn);
        }

        entitiesById.addKey(entityID, entityToSpawn);
    }

    public Entity getEntityByID(int id) {
        return id == mc.thePlayer.getEntityId() ? mc.thePlayer : super.getEntityByID(id);
    }

    public Entity removeEntityFromWorld(int entityID) {
        Entity entity = entitiesById.removeObject(entityID);

        if (entity != null) {
            entityList.remove(entity);
            removeEntity(entity);
        }

        return entity;
    }

    public boolean invalidateRegionAndSetBlock(BlockPos pos, IBlockState state) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        invalidateBlockReceiveRegion(i, j, k, i, j, k);
        return super.setBlockState(pos, state, 3);
    }

    public void sendQuittingDisconnectingPacket() {
        sendQueue.getNetworkManager().closeChannel(new ChatComponentText("Quitting"));
    }

    protected void updateWeather() {
    }

    protected int getRenderDistanceChunks() {
        return mc.gameSettings.renderDistanceChunks;
    }

    public void doVoidFogParticles(int posX, int posY, int posZ) {
        int i = 16;
        Random random = new Random();
        ItemStack itemstack = mc.thePlayer.getHeldItem();
        boolean flag = mc.playerController.getCurrentGameType() == WorldSettings.GameType.CREATIVE && itemstack != null && Block.getBlockFromItem(itemstack.getItem()) == Blocks.barrier;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int j = 0; j < 1000; ++j) {
            int k = posX + rand.nextInt(i) - rand.nextInt(i);
            int l = posY + rand.nextInt(i) - rand.nextInt(i);
            int i1 = posZ + rand.nextInt(i) - rand.nextInt(i);
            blockpos$mutableblockpos.set(k, l, i1);
            IBlockState iblockstate = getBlockState(blockpos$mutableblockpos);
            iblockstate.getBlock().randomDisplayTick(this, blockpos$mutableblockpos, iblockstate, random);

            if (flag && iblockstate.getBlock() == Blocks.barrier) {
                spawnParticle(EnumParticleTypes.BARRIER, (float) k + 0.5F, (float) l + 0.5F, (float) i1 + 0.5F, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public void removeAllEntities() {
        loadedEntityList.removeAll(unloadedEntityList);

        for (Entity entity : unloadedEntityList) {
            int j = entity.chunkCoordX;
            int k = entity.chunkCoordZ;

            if (entity.addedToChunk && isChunkLoaded(j, k, true)) {
                getChunkFromChunkCoords(j, k).removeEntity(entity);
            }
        }

        for (Entity entity : unloadedEntityList) {
            onEntityRemoved(entity);
        }

        unloadedEntityList.clear();

        for (int i1 = 0; i1 < loadedEntityList.size(); ++i1) {
            Entity entity1 = loadedEntityList.get(i1);

            if (entity1.ridingEntity != null) {
                if (!entity1.ridingEntity.isDead && entity1.ridingEntity.riddenByEntity == entity1) {
                    continue;
                }

                entity1.ridingEntity.riddenByEntity = null;
                entity1.ridingEntity = null;
            }

            if (entity1.isDead) {
                int j1 = entity1.chunkCoordX;
                int k1 = entity1.chunkCoordZ;

                if (entity1.addedToChunk && isChunkLoaded(j1, k1, true)) {
                    getChunkFromChunkCoords(j1, k1).removeEntity(entity1);
                }

                loadedEntityList.remove(i1--);
                onEntityRemoved(entity1);
            }
        }
    }

    public CrashReportCategory addWorldInfoToCrashReport(CrashReport report) {
        CrashReportCategory crashreportcategory = super.addWorldInfoToCrashReport(report);
        crashreportcategory.addCrashSectionCallable("Forced entities", () -> entityList.size() + " total; " + entityList);
        crashreportcategory.addCrashSectionCallable("Retry entities", () -> entitySpawnQueue.size() + " total; " + entitySpawnQueue);
        crashreportcategory.addCrashSectionCallable("Server brand", () -> mc.thePlayer.getClientBrand());
        crashreportcategory.addCrashSectionCallable("Server type", () -> mc.getIntegratedServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server");
        return crashreportcategory;
    }

    public void playSoundAtPos(BlockPos pos, String soundName, float volume, float pitch, boolean distanceDelay) {
        playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, soundName, volume, pitch, distanceDelay);
    }

    public void playSound(double x, double y, double z, String soundName, float volume, float pitch, boolean distanceDelay) {
        double d0 = mc.getRenderViewEntity().getDistanceSq(x, y, z);
        PositionedSoundRecord positionedsoundrecord = new PositionedSoundRecord(new ResourceLocation(soundName), volume, pitch, (float) x, (float) y, (float) z);

        if (distanceDelay && d0 > 100.0D) {
            double d1 = Math.sqrt(d0) / 40.0D;
            mc.getSoundHandler().playDelayedSound(positionedsoundrecord, (int) (d1 * 20.0D));
        } else {
            mc.getSoundHandler().playSound(positionedsoundrecord);
        }
    }

    public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, NBTTagCompound compund) {
        mc.effectRenderer.addEffect(new EntityFirework.StarterFX(this, x, y, z, motionX, motionY, motionZ, mc.effectRenderer, compund));
    }

    public void setWorldScoreboard(Scoreboard scoreboardIn) {
        worldScoreboard = scoreboardIn;
    }

    public void setWorldTime(long time) {
        if (time < 0L) {
            time = -time;
            getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
        } else {
            getGameRules().setOrCreateGameRule("doDaylightCycle", "true");
        }

        super.setWorldTime(time);
    }

    public int getCombinedLight(BlockPos pos, int lightValue) {
        int i = super.getCombinedLight(pos, lightValue);

        if (Config.isDynamicLights()) {
            i = DynamicLights.getCombinedLight(pos, i);
        }

        return i;
    }

    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        playerUpdate = isPlayerActing();
        boolean flag = super.setBlockState(pos, newState, flags);
        playerUpdate = false;
        return flag;
    }

    private boolean isPlayerActing() {
        if (mc.playerController instanceof PlayerControllerOF playercontrollerof) {
            return playercontrollerof.isActing();
        } else {
            return false;
        }
    }

    public boolean isPlayerUpdate() {
        return playerUpdate;
    }
}
