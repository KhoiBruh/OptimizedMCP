package net.minecraft.world;

public enum Difficulty {
    PEACEFUL(0, "options.difficulty.peaceful"),
    EASY(1, "options.difficulty.easy"),
    NORMAL(2, "options.difficulty.normal"),
    HARD(3, "options.difficulty.hard");

    private static final Difficulty[] difficultyEnums = new Difficulty[values().length];

    static {
        for (Difficulty enumdifficulty : values()) {
            difficultyEnums[enumdifficulty.difficultyId] = enumdifficulty;
        }
    }

    private final int difficultyId;
    private final String difficultyResourceKey;

    Difficulty(int difficultyIdIn, String difficultyResourceKeyIn) {
        difficultyId = difficultyIdIn;
        difficultyResourceKey = difficultyResourceKeyIn;
    }

    public static Difficulty getDifficultyEnum(int p_151523_0_) {
        return difficultyEnums[p_151523_0_ % difficultyEnums.length];
    }

    public int getDifficultyId() {
        return difficultyId;
    }

    public String getDifficultyResourceKey() {
        return difficultyResourceKey;
    }
}
