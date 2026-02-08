package net.minecraft.server.integrated;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ThreadLanServerPing;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.CryptManager;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.Util;
import net.minecraft.world.*;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.optifine.ClearWater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class IntegratedServer extends MinecraftServer {
    private static final Logger logger = LogManager.getLogger();
    private final Minecraft mc;
    private final WorldSettings theWorldSettings;
    public World difficultyUpdateWorld = null;
    public BlockPos difficultyUpdatePos = null;
    public DifficultyInstance difficultyLast = null;
    private boolean isGamePaused;
    private boolean isPublic;
    private ThreadLanServerPing lanServerPing;
    private long ticksSaveLast = 0L;

    public IntegratedServer(Minecraft mcIn) {
        super(mcIn.getProxy(), new File(mcIn.mcDataDir, USER_CACHE_FILE.getName()));
        mc = mcIn;
        theWorldSettings = null;
    }

    public IntegratedServer(Minecraft mcIn, String folderName, String worldName, WorldSettings settings) {
        super(new File(mcIn.mcDataDir, "saves"), mcIn.getProxy(), new File(mcIn.mcDataDir, USER_CACHE_FILE.getName()));
        setServerOwner(mcIn.getSession().username());
        setFolderName(folderName);
        setWorldName(worldName);
        canCreateBonusChest(settings.isBonusChestEnabled());
        setBuildLimit(256);
        setConfigManager(new IntegratedPlayerList(this));
        mc = mcIn;
        theWorldSettings = settings;
        ISaveHandler isavehandler = getActiveAnvilConverter().getSaveLoader(folderName, false);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();

        if (worldinfo != null) {
            NBTTagCompound nbttagcompound = worldinfo.getPlayerNBTTagCompound();

            if (nbttagcompound != null && nbttagcompound.hasKey("Dimension")) {
                PacketThreadUtil.lastDimensionId = nbttagcompound.getInteger("Dimension");
                mc.loadingScreen.setLoadingProgress(-1);
            }
        }
    }

    protected ServerCommandManager createNewCommandManager() {
        return new IntegratedServerCommandManager();
    }

    protected void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String worldNameIn2) {
        convertMapIfNeeded(saveName);

        worldServers = new WorldServer[3];
        timeOfLastDimensionTick = new long[worldServers.length][100];

        ISaveHandler isavehandler = getActiveAnvilConverter().getSaveLoader(saveName, true);
        setResourcePackFromWorld(getFolderName(), isavehandler);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();

        if (worldinfo == null) {
            worldinfo = new WorldInfo(theWorldSettings, worldNameIn);
        } else {
            worldinfo.setWorldName(worldNameIn);
        }

        for (int l = 0; l < worldServers.length; ++l) {
            int i1 = 0;

            if (l == 1) {
                i1 = -1;
            }

            if (l == 2) {
                i1 = 1;
            }

            if (l == 0) {
                worldServers[l] = (WorldServer) (new WorldServer(this, isavehandler, worldinfo, i1, theProfiler)).init();
                worldServers[l].initialize(theWorldSettings);
            } else {
                worldServers[l] = (WorldServer) (new WorldServerMulti(this, isavehandler, i1, worldServers[0], theProfiler)).init();
            }

            worldServers[l].addWorldAccess(new WorldManager(this, worldServers[l]));
        }

        getConfigurationManager().setPlayerManager(worldServers);

        if (worldServers[0].getWorldInfo().getDifficulty() == null) {
            setDifficultyForAllWorlds(mc.gameSettings.difficulty);
        }

        initialWorldChunkLoad();
    }

    protected boolean startServer() {
        logger.info("Starting integrated minecraft server version 1.9");
        setOnlineMode(true);
        setCanSpawnAnimals(true);
        setCanSpawnNPCs(true);
        setAllowPvp(true);
        setAllowFlight(true);
        logger.info("Generating keypair");
        setKeyPair(CryptManager.generateKeyPair());

        loadAllWorlds(getFolderName(), getWorldName(), theWorldSettings.getSeed(), theWorldSettings.getTerrainType(), theWorldSettings.getWorldName());
        setMOTD(getServerOwner() + " - " + worldServers[0].getWorldInfo().getWorldName());

        return true;
    }

    public void tick() {
        onTick();
        boolean flag = isGamePaused;
        isGamePaused = Minecraft.getMinecraft().getNetHandler() != null && Minecraft.getMinecraft().isGamePaused();

        if (!flag && isGamePaused) {
            logger.info("Saving and pausing game...");
            getConfigurationManager().saveAllPlayerData();
            saveAllWorlds(false);
        }

        if (isGamePaused) {
            synchronized (futureTaskQueue) {
                while (!futureTaskQueue.isEmpty()) {
                    Util.runTask(futureTaskQueue.poll(), logger);
                }
            }
        } else {
            super.tick();

            if (mc.gameSettings.renderDistanceChunks != getConfigurationManager().getViewDistance()) {
                logger.info("Changing view distance to {}, from {}", new Object[]{mc.gameSettings.renderDistanceChunks, getConfigurationManager().getViewDistance()});
                getConfigurationManager().setViewDistance(mc.gameSettings.renderDistanceChunks);
            }

            if (mc.theWorld != null) {
                WorldInfo worldinfo1 = worldServers[0].getWorldInfo();
                WorldInfo worldinfo = mc.theWorld.getWorldInfo();

                if (!worldinfo1.isDifficultyLocked() && worldinfo.getDifficulty() != worldinfo1.getDifficulty()) {
                    logger.info("Changing difficulty to {}, from {}", new Object[]{worldinfo.getDifficulty(), worldinfo1.getDifficulty()});
                    setDifficultyForAllWorlds(worldinfo.getDifficulty());
                } else if (worldinfo.isDifficultyLocked() && !worldinfo1.isDifficultyLocked()) {
                    logger.info("Locking difficulty to {}", new Object[]{worldinfo.getDifficulty()});

                    for (WorldServer worldserver : worldServers) {
                        if (worldserver != null) {
                            worldserver.getWorldInfo().setDifficultyLocked(true);
                        }
                    }
                }
            }
        }
    }

    public boolean canStructuresSpawn() {
        return false;
    }

    public WorldSettings.GameType getGameType() {
        return theWorldSettings.getGameType();
    }

    public void setGameType(WorldSettings.GameType gameMode) {
        getConfigurationManager().setGameType(gameMode);
    }

    public Difficulty getDifficulty() {
        return mc.theWorld == null ? mc.gameSettings.difficulty : mc.theWorld.getWorldInfo().getDifficulty();
    }

    public boolean isHardcore() {
        return theWorldSettings.getHardcoreEnabled();
    }

    public boolean shouldBroadcastRconToOps() {
        return true;
    }

    public boolean shouldBroadcastConsoleToOps() {
        return true;
    }

    public void saveAllWorlds(boolean dontLog) {
        if (dontLog) {
            int i = getTickCounter();
            int j = mc.gameSettings.ofAutoSaveTicks;

            if ((long) i < ticksSaveLast + (long) j) {
                return;
            }

            ticksSaveLast = i;
        }

        super.saveAllWorlds(dontLog);
    }

    public File getDataDirectory() {
        return mc.mcDataDir;
    }

    public boolean isDedicatedServer() {
        return false;
    }

    public boolean shouldUseNativeTransport() {
        return false;
    }

    protected void finalTick(CrashReport report) {
        mc.crashed(report);
    }

    public CrashReport addServerInfoToCrashReport(CrashReport report) {
        report = super.addServerInfoToCrashReport(report);
        report.getCategory().addCrashSectionCallable("Type", () -> "Integrated Server (map_client.txt)");
        report.getCategory().addCrashSectionCallable("Is Modded", () -> {
            String s = ClientBrandRetriever.getClientModName();

            if (!s.equals("vanilla")) {
                return "Definitely; Client brand changed to '" + s + "'";
            } else {
                s = getServerModName();
                return !s.equals("vanilla") ? "Definitely; Server brand changed to '" + s + "'" : (Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and both client + server brands are untouched.");
            }
        });
        return report;
    }

    public void setDifficultyForAllWorlds(Difficulty difficulty) {
        super.setDifficultyForAllWorlds(difficulty);

        if (mc.theWorld != null) {
            mc.theWorld.getWorldInfo().setDifficulty(difficulty);
        }
    }

    public String shareToLAN(WorldSettings.GameType type, boolean allowCheats) {
        try {
            int i = -1;

            try {
                i = HttpUtil.getSuitableLanPort();
            } catch (IOException var5) {
            }

            if (i <= 0) {
                i = 25564;
            }

            getNetworkSystem().addLanEndpoint(null, i);
            logger.info("Started on {}", i);
            isPublic = true;
            lanServerPing = new ThreadLanServerPing(getMOTD(), i + "");
            lanServerPing.start();
            getConfigurationManager().setGameType(type);
            getConfigurationManager().setCommandsAllowedForAll(allowCheats);
            return i + "";
        } catch (IOException var6) {
            return null;
        }
    }

    public void stopServer() {
        super.stopServer();

        if (lanServerPing != null) {
            lanServerPing.interrupt();
            lanServerPing = null;
        }
    }

    public void initiateShutdown() {
        if (isServerRunning()) {
            Futures.getUnchecked(addScheduledTask(() -> {
                for (EntityPlayerMP entityplayermp : Lists.newArrayList(getConfigurationManager().getPlayerList())) {
                    getConfigurationManager().playerLoggedOut(entityplayermp);
                }
            }));
        }

        super.initiateShutdown();

        if (lanServerPing != null) {
            lanServerPing.interrupt();
            lanServerPing = null;
        }
    }

    public void setStaticInstance() {
        setInstance();
    }

    public boolean getPublic() {
        return isPublic;
    }

    public boolean isCommandBlockEnabled() {
        return true;
    }

    public int getOpPermissionLevel() {
        return 4;
    }

    private void onTick() {
        for (WorldServer worldserver : worldServers) {
            onTick(worldserver);
        }
    }

    public DifficultyInstance getDifficultyAsync(World p_getDifficultyAsync_1_, BlockPos p_getDifficultyAsync_2_) {
        difficultyUpdateWorld = p_getDifficultyAsync_1_;
        difficultyUpdatePos = p_getDifficultyAsync_2_;
        return difficultyLast;
    }

    private void onTick(WorldServer p_onTick_1_) {
        if (!Config.isTimeDefault()) {
            fixWorldTime(p_onTick_1_);
        }

        if (!Config.isWeatherEnabled()) {
            fixWorldWeather(p_onTick_1_);
        }

        if (Config.waterOpacityChanged) {
            Config.waterOpacityChanged = false;
            ClearWater.updateWaterOpacity(Config.getGameSettings(), p_onTick_1_);
        }

        if (difficultyUpdateWorld == p_onTick_1_ && difficultyUpdatePos != null) {
            difficultyLast = p_onTick_1_.getDifficultyForLocation(difficultyUpdatePos);
            difficultyUpdateWorld = null;
            difficultyUpdatePos = null;
        }
    }

    private void fixWorldWeather(WorldServer p_fixWorldWeather_1_) {
        WorldInfo worldinfo = p_fixWorldWeather_1_.getWorldInfo();

        if (worldinfo.isRaining() || worldinfo.isThundering()) {
            worldinfo.setRainTime(0);
            worldinfo.setRaining(false);
            p_fixWorldWeather_1_.setRainStrength(0.0F);
            worldinfo.setThunderTime(0);
            worldinfo.setThundering(false);
            p_fixWorldWeather_1_.setThunderStrength(0.0F);
            getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(2, 0.0F));
            getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(7, 0.0F));
            getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(8, 0.0F));
        }
    }

    private void fixWorldTime(WorldServer p_fixWorldTime_1_) {
        WorldInfo worldinfo = p_fixWorldTime_1_.getWorldInfo();

        if (worldinfo.getGameType().getID() == 1) {
            long i = p_fixWorldTime_1_.getWorldTime();
            long j = i % 24000L;

            if (Config.isTimeDayOnly()) {
                if (j <= 1000L) {
                    p_fixWorldTime_1_.setWorldTime(i - j + 1001L);
                }

                if (j >= 11000L) {
                    p_fixWorldTime_1_.setWorldTime(i - j + 24001L);
                }
            }

            if (Config.isTimeNightOnly()) {
                if (j <= 14000L) {
                    p_fixWorldTime_1_.setWorldTime(i - j + 14001L);
                }

                if (j >= 22000L) {
                    p_fixWorldTime_1_.setWorldTime(i - j + 24000L + 14001L);
                }
            }
        }
    }
}
