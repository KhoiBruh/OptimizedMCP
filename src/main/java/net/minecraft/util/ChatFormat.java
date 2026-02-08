package net.minecraft.util;

import java.util.*;
import java.util.regex.Pattern;

public enum ChatFormat {
    BLACK("BLACK", '0', 0),
    DARK_BLUE("DARK_BLUE", '1', 1),
    DARK_GREEN("DARK_GREEN", '2', 2),
    DARK_AQUA("DARK_AQUA", '3', 3),
    DARK_RED("DARK_RED", '4', 4),
    DARK_PURPLE("DARK_PURPLE", '5', 5),
    GOLD("GOLD", '6', 6),
    GRAY("GRAY", '7', 7),
    DARK_GRAY("DARK_GRAY", '8', 8),
    BLUE("BLUE", '9', 9),
    GREEN("GREEN", 'a', 10),
    AQUA("AQUA", 'b', 11),
    RED("RED", 'c', 12),
    LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13),
    YELLOW("YELLOW", 'e', 14),
    WHITE("WHITE", 'f', 15),
    OBFUSCATED("OBFUSCATED", 'k', true),
    BOLD("BOLD", 'l', true),
    STRIKETHROUGH("STRIKETHROUGH", 'm', true),
    UNDERLINE("UNDERLINE", 'n', true),
    ITALIC("ITALIC", 'o', true),
    RESET("RESET", 'r', -1);

    private static final Map<String, ChatFormat> nameMapping = new HashMap<>();
    private static final Pattern formattingCodePattern = Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]");

    static {
        for (ChatFormat enumchatformatting : values()) {
            nameMapping.put(func_175745_c(enumchatformatting.name), enumchatformatting);
        }
    }

    private final String name;
    private final char formattingCode;
    private final boolean fancyStyling;
    private final String controlString;
    private final int colorIndex;

    ChatFormat(String formattingName, char formattingCodeIn, int colorIndex) {
        this(formattingName, formattingCodeIn, false, colorIndex);
    }

    ChatFormat(String formattingName, char formattingCodeIn, boolean fancyStylingIn) {
        this(formattingName, formattingCodeIn, fancyStylingIn, -1);
    }

    ChatFormat(String formattingName, char formattingCodeIn, boolean fancyStylingIn, int colorIndex) {
        name = formattingName;
        formattingCode = formattingCodeIn;
        fancyStyling = fancyStylingIn;
        this.colorIndex = colorIndex;
        controlString = "§" + formattingCodeIn;
    }

    private static String func_175745_c(String p_175745_0_) {
        return p_175745_0_.toLowerCase().replaceAll("[^a-z]", "");
    }

    public static String getTextWithoutFormattingCodes(String text) {
        return text == null ? null : formattingCodePattern.matcher(text).replaceAll("");
    }

    public static ChatFormat getValueByName(String friendlyName) {
        return friendlyName == null ? null : nameMapping.get(func_175745_c(friendlyName));
    }

    public static ChatFormat func_175744_a(int p_175744_0_) {
        if (p_175744_0_ < 0) {
            return RESET;
        } else {
            for (ChatFormat enumchatformatting : values()) {
                if (enumchatformatting.colorIndex == p_175744_0_) {
                    return enumchatformatting;
                }
            }

            return null;
        }
    }

    public static Collection<String> getValidValues(boolean p_96296_0_, boolean p_96296_1_) {
        List<String> list = new ArrayList<>();

        for (ChatFormat enumchatformatting : values()) {
            if ((!enumchatformatting.isColor() || p_96296_0_) && (!enumchatformatting.fancyStyling || p_96296_1_)) {
                list.add(enumchatformatting.getFriendlyName());
            }
        }

        return list;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public boolean isFancyStyling() {
        return fancyStyling;
    }

    public boolean isColor() {
        return !fancyStyling && this != RESET;
    }

    public String getFriendlyName() {
        return name().toLowerCase();
    }

    public String toString() {
        return controlString;
    }
}
