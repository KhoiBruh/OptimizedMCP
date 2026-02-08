package net.minecraft.item;

import net.minecraft.util.ChatFormat;

public enum Rarity {
    COMMON(ChatFormat.WHITE, "Common"),
    UNCOMMON(ChatFormat.YELLOW, "Uncommon"),
    RARE(ChatFormat.AQUA, "Rare"),
    EPIC(ChatFormat.LIGHT_PURPLE, "Epic");

    public final ChatFormat rarityColor;
    public final String rarityName;

    Rarity(ChatFormat color, String name) {
        rarityColor = color;
        rarityName = name;
    }
}
