package net.minecraft.item;

import net.minecraft.block.material.MapColor;
import net.minecraft.util.ChatFormat;
import net.minecraft.util.IStringSerializable;

public enum DyeColor implements IStringSerializable {
    WHITE(0, 15, "white", "white", MapColor.snowColor, ChatFormat.WHITE),
    ORANGE(1, 14, "orange", "orange", MapColor.adobeColor, ChatFormat.GOLD),
    MAGENTA(2, 13, "magenta", "magenta", MapColor.magentaColor, ChatFormat.AQUA),
    LIGHT_BLUE(3, 12, "light_blue", "lightBlue", MapColor.lightBlueColor, ChatFormat.BLUE),
    YELLOW(4, 11, "yellow", "yellow", MapColor.yellowColor, ChatFormat.YELLOW),
    LIME(5, 10, "lime", "lime", MapColor.limeColor, ChatFormat.GREEN),
    PINK(6, 9, "pink", "pink", MapColor.pinkColor, ChatFormat.LIGHT_PURPLE),
    GRAY(7, 8, "gray", "gray", MapColor.grayColor, ChatFormat.DARK_GRAY),
    SILVER(8, 7, "silver", "silver", MapColor.silverColor, ChatFormat.GRAY),
    CYAN(9, 6, "cyan", "cyan", MapColor.cyanColor, ChatFormat.DARK_AQUA),
    PURPLE(10, 5, "purple", "purple", MapColor.purpleColor, ChatFormat.DARK_PURPLE),
    BLUE(11, 4, "blue", "blue", MapColor.blueColor, ChatFormat.DARK_BLUE),
    BROWN(12, 3, "brown", "brown", MapColor.brownColor, ChatFormat.GOLD),
    GREEN(13, 2, "green", "green", MapColor.greenColor, ChatFormat.DARK_GREEN),
    RED(14, 1, "red", "red", MapColor.redColor, ChatFormat.DARK_RED),
    BLACK(15, 0, "black", "black", MapColor.blackColor, ChatFormat.BLACK);

    private static final DyeColor[] META_LOOKUP = new DyeColor[values().length];
    private static final DyeColor[] DYE_DMG_LOOKUP = new DyeColor[values().length];

    static {
        for (DyeColor enumdyecolor : values()) {
            META_LOOKUP[enumdyecolor.meta] = enumdyecolor;
            DYE_DMG_LOOKUP[enumdyecolor.dyeDamage] = enumdyecolor;
        }
    }

    private final int meta;
    private final int dyeDamage;
    private final String name;
    private final String unlocalizedName;
    private final MapColor mapColor;
    private final ChatFormat chatColor;

    DyeColor(int meta, int dyeDamage, String name, String unlocalizedName, MapColor mapColorIn, ChatFormat chatColor) {
        this.meta = meta;
        this.dyeDamage = dyeDamage;
        this.name = name;
        this.unlocalizedName = unlocalizedName;
        mapColor = mapColorIn;
        this.chatColor = chatColor;
    }

    public static DyeColor byDyeDamage(int damage) {
        if (damage < 0 || damage >= DYE_DMG_LOOKUP.length) {
            damage = 0;
        }

        return DYE_DMG_LOOKUP[damage];
    }

    public static DyeColor byMetadata(int meta) {
        if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
        }

        return META_LOOKUP[meta];
    }

    public int getMetadata() {
        return meta;
    }

    public int getDyeDamage() {
        return dyeDamage;
    }

    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public MapColor getMapColor() {
        return mapColor;
    }

    public String toString() {
        return unlocalizedName;
    }

    public String getName() {
        return name;
    }
}
