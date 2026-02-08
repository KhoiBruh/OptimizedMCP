package net.minecraft.world.storage;

import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

public class WorldInfo {
    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;
    private long randomSeed;
    private WorldType terrainType = WorldType.DEFAULT;
    private String generatorOptions = "";
    private int spawnX;
    private int spawnY;
    private int spawnZ;
    private long totalTime;
    private long worldTime;
    private long lastTimePlayed;
    private long sizeOnDisk;
    private NBTTagCompound playerTag;
    private int dimension;
    private String levelName;
    private int saveVersion;
    private int cleanWeatherTime;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;
    private WorldSettings.GameType theGameType;
    private boolean mapFeaturesEnabled;
    private boolean hardcore;
    private boolean allowCommands;
    private boolean initialized;
    private Difficulty difficulty;
    private boolean difficultyLocked;
    private double borderCenterX = 0.0D;
    private double borderCenterZ = 0.0D;
    private double borderSize = 6.0E7D;
    private long borderSizeLerpTime = 0L;
    private double borderSizeLerpTarget = 0.0D;
    private double borderSafeZone = 5.0D;
    private double borderDamagePerBlock = 0.2D;
    private int borderWarningDistance = 5;
    private int borderWarningTime = 15;
    private GameRules theGameRules = new GameRules();

    protected WorldInfo() {
    }

    public WorldInfo(NBTTagCompound nbt) {
        randomSeed = nbt.getLong("RandomSeed");

        if (nbt.hasKey("generatorName", 8)) {
            String s = nbt.getString("generatorName");
            terrainType = WorldType.parseWorldType(s);

            if (terrainType == null) {
                terrainType = WorldType.DEFAULT;
            } else if (terrainType.isVersioned()) {
                int i = 0;

                if (nbt.hasKey("generatorVersion", 99)) {
                    i = nbt.getInteger("generatorVersion");
                }

                terrainType = terrainType.getWorldTypeForGeneratorVersion(i);
            }

            if (nbt.hasKey("generatorOptions", 8)) {
                generatorOptions = nbt.getString("generatorOptions");
            }
        }

        theGameType = WorldSettings.GameType.getByID(nbt.getInteger("GameType"));

        if (nbt.hasKey("MapFeatures", 99)) {
            mapFeaturesEnabled = nbt.getBoolean("MapFeatures");
        } else {
            mapFeaturesEnabled = true;
        }

        spawnX = nbt.getInteger("SpawnX");
        spawnY = nbt.getInteger("SpawnY");
        spawnZ = nbt.getInteger("SpawnZ");
        totalTime = nbt.getLong("Time");

        if (nbt.hasKey("DayTime", 99)) {
            worldTime = nbt.getLong("DayTime");
        } else {
            worldTime = totalTime;
        }

        lastTimePlayed = nbt.getLong("LastPlayed");
        sizeOnDisk = nbt.getLong("SizeOnDisk");
        levelName = nbt.getString("LevelName");
        saveVersion = nbt.getInteger("version");
        cleanWeatherTime = nbt.getInteger("clearWeatherTime");
        rainTime = nbt.getInteger("rainTime");
        raining = nbt.getBoolean("raining");
        thunderTime = nbt.getInteger("thunderTime");
        thundering = nbt.getBoolean("thundering");
        hardcore = nbt.getBoolean("hardcore");

        if (nbt.hasKey("initialized", 99)) {
            initialized = nbt.getBoolean("initialized");
        } else {
            initialized = true;
        }

        if (nbt.hasKey("allowCommands", 99)) {
            allowCommands = nbt.getBoolean("allowCommands");
        } else {
            allowCommands = theGameType == WorldSettings.GameType.CREATIVE;
        }

        if (nbt.hasKey("Player", 10)) {
            playerTag = nbt.getCompoundTag("Player");
            dimension = playerTag.getInteger("Dimension");
        }

        if (nbt.hasKey("GameRules", 10)) {
            theGameRules.readFromNBT(nbt.getCompoundTag("GameRules"));
        }

        if (nbt.hasKey("Difficulty", 99)) {
            difficulty = Difficulty.getDifficultyEnum(nbt.getByte("Difficulty"));
        }

        if (nbt.hasKey("DifficultyLocked", 1)) {
            difficultyLocked = nbt.getBoolean("DifficultyLocked");
        }

        if (nbt.hasKey("BorderCenterX", 99)) {
            borderCenterX = nbt.getDouble("BorderCenterX");
        }

        if (nbt.hasKey("BorderCenterZ", 99)) {
            borderCenterZ = nbt.getDouble("BorderCenterZ");
        }

        if (nbt.hasKey("BorderSize", 99)) {
            borderSize = nbt.getDouble("BorderSize");
        }

        if (nbt.hasKey("BorderSizeLerpTime", 99)) {
            borderSizeLerpTime = nbt.getLong("BorderSizeLerpTime");
        }

        if (nbt.hasKey("BorderSizeLerpTarget", 99)) {
            borderSizeLerpTarget = nbt.getDouble("BorderSizeLerpTarget");
        }

        if (nbt.hasKey("BorderSafeZone", 99)) {
            borderSafeZone = nbt.getDouble("BorderSafeZone");
        }

        if (nbt.hasKey("BorderDamagePerBlock", 99)) {
            borderDamagePerBlock = nbt.getDouble("BorderDamagePerBlock");
        }

        if (nbt.hasKey("BorderWarningBlocks", 99)) {
            borderWarningDistance = nbt.getInteger("BorderWarningBlocks");
        }

        if (nbt.hasKey("BorderWarningTime", 99)) {
            borderWarningTime = nbt.getInteger("BorderWarningTime");
        }
    }

    public WorldInfo(WorldSettings settings, String name) {
        populateFromWorldSettings(settings);
        levelName = name;
        difficulty = DEFAULT_DIFFICULTY;
        initialized = false;
    }

    public WorldInfo(WorldInfo worldInformation) {
        randomSeed = worldInformation.randomSeed;
        terrainType = worldInformation.terrainType;
        generatorOptions = worldInformation.generatorOptions;
        theGameType = worldInformation.theGameType;
        mapFeaturesEnabled = worldInformation.mapFeaturesEnabled;
        spawnX = worldInformation.spawnX;
        spawnY = worldInformation.spawnY;
        spawnZ = worldInformation.spawnZ;
        totalTime = worldInformation.totalTime;
        worldTime = worldInformation.worldTime;
        lastTimePlayed = worldInformation.lastTimePlayed;
        sizeOnDisk = worldInformation.sizeOnDisk;
        playerTag = worldInformation.playerTag;
        dimension = worldInformation.dimension;
        levelName = worldInformation.levelName;
        saveVersion = worldInformation.saveVersion;
        rainTime = worldInformation.rainTime;
        raining = worldInformation.raining;
        thunderTime = worldInformation.thunderTime;
        thundering = worldInformation.thundering;
        hardcore = worldInformation.hardcore;
        allowCommands = worldInformation.allowCommands;
        initialized = worldInformation.initialized;
        theGameRules = worldInformation.theGameRules;
        difficulty = worldInformation.difficulty;
        difficultyLocked = worldInformation.difficultyLocked;
        borderCenterX = worldInformation.borderCenterX;
        borderCenterZ = worldInformation.borderCenterZ;
        borderSize = worldInformation.borderSize;
        borderSizeLerpTime = worldInformation.borderSizeLerpTime;
        borderSizeLerpTarget = worldInformation.borderSizeLerpTarget;
        borderSafeZone = worldInformation.borderSafeZone;
        borderDamagePerBlock = worldInformation.borderDamagePerBlock;
        borderWarningTime = worldInformation.borderWarningTime;
        borderWarningDistance = worldInformation.borderWarningDistance;
    }

    public void populateFromWorldSettings(WorldSettings settings) {
        randomSeed = settings.getSeed();
        theGameType = settings.getGameType();
        mapFeaturesEnabled = settings.isMapFeaturesEnabled();
        hardcore = settings.getHardcoreEnabled();
        terrainType = settings.getTerrainType();
        generatorOptions = settings.getWorldName();
        allowCommands = settings.areCommandsAllowed();
    }

    public NBTTagCompound getNBTTagCompound() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        updateTagCompound(nbttagcompound, playerTag);
        return nbttagcompound;
    }

    public NBTTagCompound cloneNBTCompound(NBTTagCompound nbt) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        updateTagCompound(nbttagcompound, nbt);
        return nbttagcompound;
    }

    private void updateTagCompound(NBTTagCompound nbt, NBTTagCompound playerNbt) {
        nbt.setLong("RandomSeed", randomSeed);
        nbt.setString("generatorName", terrainType.getWorldTypeName());
        nbt.setInteger("generatorVersion", terrainType.getGeneratorVersion());
        nbt.setString("generatorOptions", generatorOptions);
        nbt.setInteger("GameType", theGameType.getID());
        nbt.setBoolean("MapFeatures", mapFeaturesEnabled);
        nbt.setInteger("SpawnX", spawnX);
        nbt.setInteger("SpawnY", spawnY);
        nbt.setInteger("SpawnZ", spawnZ);
        nbt.setLong("Time", totalTime);
        nbt.setLong("DayTime", worldTime);
        nbt.setLong("SizeOnDisk", sizeOnDisk);
        nbt.setLong("LastPlayed", MinecraftServer.getCurrentTimeMillis());
        nbt.setString("LevelName", levelName);
        nbt.setInteger("version", saveVersion);
        nbt.setInteger("clearWeatherTime", cleanWeatherTime);
        nbt.setInteger("rainTime", rainTime);
        nbt.setBoolean("raining", raining);
        nbt.setInteger("thunderTime", thunderTime);
        nbt.setBoolean("thundering", thundering);
        nbt.setBoolean("hardcore", hardcore);
        nbt.setBoolean("allowCommands", allowCommands);
        nbt.setBoolean("initialized", initialized);
        nbt.setDouble("BorderCenterX", borderCenterX);
        nbt.setDouble("BorderCenterZ", borderCenterZ);
        nbt.setDouble("BorderSize", borderSize);
        nbt.setLong("BorderSizeLerpTime", borderSizeLerpTime);
        nbt.setDouble("BorderSafeZone", borderSafeZone);
        nbt.setDouble("BorderDamagePerBlock", borderDamagePerBlock);
        nbt.setDouble("BorderSizeLerpTarget", borderSizeLerpTarget);
        nbt.setDouble("BorderWarningBlocks", borderWarningDistance);
        nbt.setDouble("BorderWarningTime", borderWarningTime);

        if (difficulty != null) {
            nbt.setByte("Difficulty", (byte) difficulty.getDifficultyId());
        }

        nbt.setBoolean("DifficultyLocked", difficultyLocked);
        nbt.setTag("GameRules", theGameRules.writeToNBT());

        if (playerNbt != null) {
            nbt.setTag("Player", playerNbt);
        }
    }

    public long getSeed() {
        return randomSeed;
    }

    public int getSpawnX() {
        return spawnX;
    }

    public void setSpawnX(int x) {
        spawnX = x;
    }

    public int getSpawnY() {
        return spawnY;
    }

    public void setSpawnY(int y) {
        spawnY = y;
    }

    public int getSpawnZ() {
        return spawnZ;
    }

    public void setSpawnZ(int z) {
        spawnZ = z;
    }

    public long getWorldTotalTime() {
        return totalTime;
    }

    public void setWorldTotalTime(long time) {
        totalTime = time;
    }

    public long getWorldTime() {
        return worldTime;
    }

    public void setWorldTime(long time) {
        worldTime = time;
    }

    public long getSizeOnDisk() {
        return sizeOnDisk;
    }

    public NBTTagCompound getPlayerNBTTagCompound() {
        return playerTag;
    }

    public void setSpawn(BlockPos spawnPoint) {
        spawnX = spawnPoint.getX();
        spawnY = spawnPoint.getY();
        spawnZ = spawnPoint.getZ();
    }

    public String getWorldName() {
        return levelName;
    }

    public void setWorldName(String worldName) {
        levelName = worldName;
    }

    public int getSaveVersion() {
        return saveVersion;
    }

    public void setSaveVersion(int version) {
        saveVersion = version;
    }

    public long getLastTimePlayed() {
        return lastTimePlayed;
    }

    public int getCleanWeatherTime() {
        return cleanWeatherTime;
    }

    public void setCleanWeatherTime(int cleanWeatherTimeIn) {
        cleanWeatherTime = cleanWeatherTimeIn;
    }

    public boolean isThundering() {
        return thundering;
    }

    public void setThundering(boolean thunderingIn) {
        thundering = thunderingIn;
    }

    public int getThunderTime() {
        return thunderTime;
    }

    public void setThunderTime(int time) {
        thunderTime = time;
    }

    public boolean isRaining() {
        return raining;
    }

    public void setRaining(boolean isRaining) {
        raining = isRaining;
    }

    public int getRainTime() {
        return rainTime;
    }

    public void setRainTime(int time) {
        rainTime = time;
    }

    public WorldSettings.GameType getGameType() {
        return theGameType;
    }

    public void setGameType(WorldSettings.GameType type) {
        theGameType = type;
    }

    public boolean isMapFeaturesEnabled() {
        return mapFeaturesEnabled;
    }

    public void setMapFeaturesEnabled(boolean enabled) {
        mapFeaturesEnabled = enabled;
    }

    public boolean isHardcoreModeEnabled() {
        return hardcore;
    }

    public void setHardcore(boolean hardcoreIn) {
        hardcore = hardcoreIn;
    }

    public WorldType getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(WorldType type) {
        terrainType = type;
    }

    public String getGeneratorOptions() {
        return generatorOptions;
    }

    public boolean areCommandsAllowed() {
        return allowCommands;
    }

    public void setAllowCommands(boolean allow) {
        allowCommands = allow;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setServerInitialized(boolean initializedIn) {
        initialized = initializedIn;
    }

    public GameRules getGameRulesInstance() {
        return theGameRules;
    }

    public double getBorderCenterX() {
        return borderCenterX;
    }

    public double getBorderCenterZ() {
        return borderCenterZ;
    }

    public double getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(double size) {
        borderSize = size;
    }

    public long getBorderLerpTime() {
        return borderSizeLerpTime;
    }

    public void setBorderLerpTime(long time) {
        borderSizeLerpTime = time;
    }

    public double getBorderLerpTarget() {
        return borderSizeLerpTarget;
    }

    public void setBorderLerpTarget(double lerpSize) {
        borderSizeLerpTarget = lerpSize;
    }

    public void getBorderCenterZ(double posZ) {
        borderCenterZ = posZ;
    }

    public void getBorderCenterX(double posX) {
        borderCenterX = posX;
    }

    public double getBorderSafeZone() {
        return borderSafeZone;
    }

    public void setBorderSafeZone(double amount) {
        borderSafeZone = amount;
    }

    public double getBorderDamagePerBlock() {
        return borderDamagePerBlock;
    }

    public void setBorderDamagePerBlock(double damage) {
        borderDamagePerBlock = damage;
    }

    public int getBorderWarningDistance() {
        return borderWarningDistance;
    }

    public void setBorderWarningDistance(int amountOfBlocks) {
        borderWarningDistance = amountOfBlocks;
    }

    public int getBorderWarningTime() {
        return borderWarningTime;
    }

    public void setBorderWarningTime(int ticks) {
        borderWarningTime = ticks;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty newDifficulty) {
        difficulty = newDifficulty;
    }

    public boolean isDifficultyLocked() {
        return difficultyLocked;
    }

    public void setDifficultyLocked(boolean locked) {
        difficultyLocked = locked;
    }

    public void addToCrashReport(CrashReportCategory category) {
        category.addCrashSectionCallable("Level seed", () -> String.valueOf(getSeed()));
        category.addCrashSectionCallable("Level generator", () -> String.format("ID %02d - %s, ver %d. Features enabled: %b", terrainType.getWorldTypeID(), terrainType.getWorldTypeName(), terrainType.getGeneratorVersion(), mapFeaturesEnabled));
        category.addCrashSectionCallable("Level generator options", () -> generatorOptions);
        category.addCrashSectionCallable("Level spawn location", () -> CrashReportCategory.getCoordinateInfo(spawnX, spawnY, spawnZ));
        category.addCrashSectionCallable("Level time", () -> String.format("%d game time, %d day time", totalTime, worldTime));
        category.addCrashSectionCallable("Level dimension", () -> String.valueOf(dimension));
        category.addCrashSectionCallable("Level storage version", () -> {
            String s = "Unknown?";

            s = switch (saveVersion) {
                case 19132 -> "McRegion";
                case 19133 -> "Anvil";
                default -> s;
            };

            return String.format("0x%05X - %s", saveVersion, s);
        });
        category.addCrashSectionCallable("Level weather", () -> String.format("Rain time: %d (now: %b), thunder time: %d (now: %b)", rainTime, raining, thunderTime, thundering));
        category.addCrashSectionCallable("Level game mode", () -> String.format("Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", theGameType.getName(), theGameType.getID(), hardcore, allowCommands));
    }
}
