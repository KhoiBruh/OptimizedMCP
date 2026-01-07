package net.minecraft.world.storage;

import net.minecraft.world.WorldSettings;

public class SaveFormatComparator implements Comparable<SaveFormatComparator> {
    private final String fileName;
    private final String displayName;
    private final long lastTimePlayed;
    private final long sizeOnDisk;
    private final boolean requiresConversion;
    private final WorldSettings.GameType theEnumGameType;
    private final boolean hardcore;
    private final boolean cheatsEnabled;

    public SaveFormatComparator(String fileNameIn, String displayNameIn, long lastTimePlayedIn, long sizeOnDiskIn, WorldSettings.GameType theEnumGameTypeIn, boolean requiresConversionIn, boolean hardcoreIn, boolean cheatsEnabledIn) {
        fileName = fileNameIn;
        displayName = displayNameIn;
        lastTimePlayed = lastTimePlayedIn;
        sizeOnDisk = sizeOnDiskIn;
        theEnumGameType = theEnumGameTypeIn;
        requiresConversion = requiresConversionIn;
        hardcore = hardcoreIn;
        cheatsEnabled = cheatsEnabledIn;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getSizeOnDisk() {
        return sizeOnDisk;
    }

    public boolean requiresConversion() {
        return requiresConversion;
    }

    public long getLastTimePlayed() {
        return lastTimePlayed;
    }

    public int compareTo(SaveFormatComparator p_compareTo_1_) {
        return lastTimePlayed < p_compareTo_1_.lastTimePlayed ? 1 : (lastTimePlayed > p_compareTo_1_.lastTimePlayed ? -1 : fileName.compareTo(p_compareTo_1_.fileName));
    }

    public WorldSettings.GameType getEnumGameType() {
        return theEnumGameType;
    }

    public boolean isHardcoreModeEnabled() {
        return hardcore;
    }

    public boolean getCheatsEnabled() {
        return cheatsEnabled;
    }
}
