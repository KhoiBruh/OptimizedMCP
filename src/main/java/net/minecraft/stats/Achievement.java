package net.minecraft.stats;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

public class Achievement extends StatBase {
    public final int displayColumn;
    public final int displayRow;
    public final Achievement parentAchievement;
    public final ItemStack theItemStack;
    private final String achievementDescription;
    private IStatStringFormat statStringFormatter;
    private boolean isSpecial;

    public Achievement(String statIdIn, String unlocalizedName, int column, int row, Item itemIn, Achievement parent) {
        this(statIdIn, unlocalizedName, column, row, new ItemStack(itemIn), parent);
    }

    public Achievement(String statIdIn, String unlocalizedName, int column, int row, Block blockIn, Achievement parent) {
        this(statIdIn, unlocalizedName, column, row, new ItemStack(blockIn), parent);
    }

    public Achievement(String statIdIn, String unlocalizedName, int column, int row, ItemStack stack, Achievement parent) {
        super(statIdIn, new ChatComponentTranslation("achievement." + unlocalizedName));
        theItemStack = stack;
        achievementDescription = "achievement." + unlocalizedName + ".desc";
        displayColumn = column;
        displayRow = row;

        if (column < AchievementList.minDisplayColumn) {
            AchievementList.minDisplayColumn = column;
        }

        if (row < AchievementList.minDisplayRow) {
            AchievementList.minDisplayRow = row;
        }

        if (column > AchievementList.maxDisplayColumn) {
            AchievementList.maxDisplayColumn = column;
        }

        if (row > AchievementList.maxDisplayRow) {
            AchievementList.maxDisplayRow = row;
        }

        parentAchievement = parent;
    }

    public Achievement initIndependentStat() {
        isIndependent = true;
        return this;
    }

    public Achievement setSpecial() {
        isSpecial = true;
        return this;
    }

    public Achievement registerStat() {
        super.registerStat();
        AchievementList.achievementList.add(this);
        return this;
    }

    public boolean isAchievement() {
        return true;
    }

    public IChatComponent getStatName() {
        IChatComponent ichatcomponent = super.getStatName();
        ichatcomponent.getChatStyle().setColor(isSpecial ? EnumChatFormatting.DARK_PURPLE : EnumChatFormatting.GREEN);
        return ichatcomponent;
    }

    public Achievement func_150953_b(Class<? extends IJsonSerializable> p_150953_1_) {
        return (Achievement) super.func_150953_b(p_150953_1_);
    }

    public String getDescription() {
        return statStringFormatter != null ? statStringFormatter.formatString(StatCollector.translateToLocal(achievementDescription)) : StatCollector.translateToLocal(achievementDescription);
    }

    public Achievement setStatStringFormatter(IStatStringFormat statStringFormatterIn) {
        statStringFormatter = statStringFormatterIn;
        return this;
    }

    public boolean getSpecial() {
        return isSpecial;
    }
}
