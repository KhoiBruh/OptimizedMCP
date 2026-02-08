package net.minecraft.scoreboard;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatFormat;

import java.util.List;

public class GoalColor implements IScoreObjectiveCriteria {
    private final String goalName;

    public GoalColor(String p_i45549_1_, ChatFormat p_i45549_2_) {
        goalName = p_i45549_1_ + p_i45549_2_.getFriendlyName();
        IScoreObjectiveCriteria.INSTANCES.put(goalName, this);
    }

    public String getName() {
        return goalName;
    }

    public int setScore(List<EntityPlayer> p_96635_1_) {
        return 0;
    }

    public boolean isReadOnly() {
        return false;
    }

    public RenderType getRenderType() {
        return RenderType.INTEGER;
    }
}
