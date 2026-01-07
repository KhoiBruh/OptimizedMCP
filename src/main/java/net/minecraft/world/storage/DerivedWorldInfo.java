package net.minecraft.world.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

public class DerivedWorldInfo extends WorldInfo {
    private final WorldInfo theWorldInfo;

    public DerivedWorldInfo(WorldInfo p_i2145_1_) {
        theWorldInfo = p_i2145_1_;
    }

    public NBTTagCompound getNBTTagCompound() {
        return theWorldInfo.getNBTTagCompound();
    }

    public NBTTagCompound cloneNBTCompound(NBTTagCompound nbt) {
        return theWorldInfo.cloneNBTCompound(nbt);
    }

    public long getSeed() {
        return theWorldInfo.getSeed();
    }

    public int getSpawnX() {
        return theWorldInfo.getSpawnX();
    }

    public void setSpawnX(int x) {
    }

    public int getSpawnY() {
        return theWorldInfo.getSpawnY();
    }

    public void setSpawnY(int y) {
    }

    public int getSpawnZ() {
        return theWorldInfo.getSpawnZ();
    }

    public void setSpawnZ(int z) {
    }

    public long getWorldTotalTime() {
        return theWorldInfo.getWorldTotalTime();
    }

    public void setWorldTotalTime(long time) {
    }

    public long getWorldTime() {
        return theWorldInfo.getWorldTime();
    }

    public void setWorldTime(long time) {
    }

    public long getSizeOnDisk() {
        return theWorldInfo.getSizeOnDisk();
    }

    public NBTTagCompound getPlayerNBTTagCompound() {
        return theWorldInfo.getPlayerNBTTagCompound();
    }

    public String getWorldName() {
        return theWorldInfo.getWorldName();
    }

    public void setWorldName(String worldName) {
    }

    public int getSaveVersion() {
        return theWorldInfo.getSaveVersion();
    }

    public void setSaveVersion(int version) {
    }

    public long getLastTimePlayed() {
        return theWorldInfo.getLastTimePlayed();
    }

    public boolean isThundering() {
        return theWorldInfo.isThundering();
    }

    public void setThundering(boolean thunderingIn) {
    }

    public int getThunderTime() {
        return theWorldInfo.getThunderTime();
    }

    public void setThunderTime(int time) {
    }

    public boolean isRaining() {
        return theWorldInfo.isRaining();
    }

    public void setRaining(boolean isRaining) {
    }

    public int getRainTime() {
        return theWorldInfo.getRainTime();
    }

    public void setRainTime(int time) {
    }

    public WorldSettings.GameType getGameType() {
        return theWorldInfo.getGameType();
    }

    public void setSpawn(BlockPos spawnPoint) {
    }

    public boolean isMapFeaturesEnabled() {
        return theWorldInfo.isMapFeaturesEnabled();
    }

    public boolean isHardcoreModeEnabled() {
        return theWorldInfo.isHardcoreModeEnabled();
    }

    public WorldType getTerrainType() {
        return theWorldInfo.getTerrainType();
    }

    public void setTerrainType(WorldType type) {
    }

    public boolean areCommandsAllowed() {
        return theWorldInfo.areCommandsAllowed();
    }

    public void setAllowCommands(boolean allow) {
    }

    public boolean isInitialized() {
        return theWorldInfo.isInitialized();
    }

    public void setServerInitialized(boolean initializedIn) {
    }

    public GameRules getGameRulesInstance() {
        return theWorldInfo.getGameRulesInstance();
    }

    public EnumDifficulty getDifficulty() {
        return theWorldInfo.getDifficulty();
    }

    public void setDifficulty(EnumDifficulty newDifficulty) {
    }

    public boolean isDifficultyLocked() {
        return theWorldInfo.isDifficultyLocked();
    }

    public void setDifficultyLocked(boolean locked) {
    }
}
