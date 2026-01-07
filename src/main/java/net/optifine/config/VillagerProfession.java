package net.optifine.config;

import net.minecraft.src.Config;

public class VillagerProfession {
    private final int profession;
    private final int[] careers;

    public VillagerProfession(int profession, int[] careers) {
        this.profession = profession;
        this.careers = careers;
    }

    public boolean matches(int prof, int car) {
        return profession == prof && (careers == null || Config.equalsOne(car, careers));
    }

    private boolean hasCareer(int car) {
        return careers != null && Config.equalsOne(car, careers);
    }

    public String toString() {
        return careers == null ? "" + profession : profession + ":" + Config.arrayToString(careers);
    }
}
