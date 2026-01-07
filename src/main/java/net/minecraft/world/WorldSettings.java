package net.minecraft.world;

import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.world.storage.WorldInfo;

public final class WorldSettings {
    private final long seed;
    private final WorldSettings.GameType theGameType;
    private final boolean mapFeaturesEnabled;
    private final boolean hardcoreEnabled;
    private final WorldType terrainType;
    private boolean commandsAllowed;
    private boolean bonusChestEnabled;
    private String worldName;

    public WorldSettings(long seedIn, WorldSettings.GameType gameType, boolean enableMapFeatures, boolean hardcoreMode, WorldType worldTypeIn) {
        worldName = "";
        seed = seedIn;
        theGameType = gameType;
        mapFeaturesEnabled = enableMapFeatures;
        hardcoreEnabled = hardcoreMode;
        terrainType = worldTypeIn;
    }

    public WorldSettings(WorldInfo info) {
        this(info.getSeed(), info.getGameType(), info.isMapFeaturesEnabled(), info.isHardcoreModeEnabled(), info.getTerrainType());
    }

    public static WorldSettings.GameType getGameTypeById(int id) {
        return WorldSettings.GameType.getByID(id);
    }

    public WorldSettings enableBonusChest() {
        bonusChestEnabled = true;
        return this;
    }

    public WorldSettings enableCommands() {
        commandsAllowed = true;
        return this;
    }

    public boolean isBonusChestEnabled() {
        return bonusChestEnabled;
    }

    public long getSeed() {
        return seed;
    }

    public WorldSettings.GameType getGameType() {
        return theGameType;
    }

    public boolean getHardcoreEnabled() {
        return hardcoreEnabled;
    }

    public boolean isMapFeaturesEnabled() {
        return mapFeaturesEnabled;
    }

    public WorldType getTerrainType() {
        return terrainType;
    }

    public boolean areCommandsAllowed() {
        return commandsAllowed;
    }

    public String getWorldName() {
        return worldName;
    }

    public WorldSettings setWorldName(String name) {
        worldName = name;
        return this;
    }

    public enum GameType {
        NOT_SET(-1, ""),
        SURVIVAL(0, "survival"),
        CREATIVE(1, "creative"),
        ADVENTURE(2, "adventure"),
        SPECTATOR(3, "spectator");

        int id;
        String name;

        GameType(int typeId, String nameIn) {
            id = typeId;
            name = nameIn;
        }

        public static WorldSettings.GameType getByID(int idIn) {
            for (WorldSettings.GameType worldsettings$gametype : values()) {
                if (worldsettings$gametype.id == idIn) {
                    return worldsettings$gametype;
                }
            }

            return SURVIVAL;
        }

        public static WorldSettings.GameType getByName(String gamemodeName) {
            for (WorldSettings.GameType worldsettings$gametype : values()) {
                if (worldsettings$gametype.name.equals(gamemodeName)) {
                    return worldsettings$gametype;
                }
            }

            return SURVIVAL;
        }

        public int getID() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void configurePlayerCapabilities(PlayerCapabilities capabilities) {
            if (this == CREATIVE) {
                capabilities.allowFlying = true;
                capabilities.isCreativeMode = true;
                capabilities.disableDamage = true;
            } else if (this == SPECTATOR) {
                capabilities.allowFlying = true;
                capabilities.isCreativeMode = false;
                capabilities.disableDamage = true;
                capabilities.isFlying = true;
            } else {
                capabilities.allowFlying = false;
                capabilities.isCreativeMode = false;
                capabilities.disableDamage = false;
                capabilities.isFlying = false;
            }

            capabilities.allowEdit = !isAdventure();
        }

        public boolean isAdventure() {
            return this == ADVENTURE || this == SPECTATOR;
        }

        public boolean isCreative() {
            return this == CREATIVE;
        }

        public boolean isSurvivalOrAdventure() {
            return this == SURVIVAL || this == ADVENTURE;
        }
    }
}
